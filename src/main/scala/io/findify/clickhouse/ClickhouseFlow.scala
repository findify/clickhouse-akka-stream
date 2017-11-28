package io.findify.clickhouse

import java.io.{File, FileInputStream, FileOutputStream}

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri.Query
import akka.stream._
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.stream.stage._
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import io.findify.clickhouse.ClickhouseFlow.{Record, Status}
import io.findify.clickhouse.format.Field.Row
import io.findify.clickhouse.format.output.{JSONEachRowOutputFormat, OutputFormat}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

case class ClickhouseFlow[T](host: String, port: Int, table: String, format: OutputFormat = new JSONEachRowOutputFormat(), maxRowsInBuffer: Int = 2048)
                    (
                      implicit val system: ActorSystem,
                      mat: Materializer
                    ) extends GraphStage[FlowShape[Record[T], Status[T]]] with ClickhouseStream {
  val in: Inlet[Record[T]] = Inlet("input")
  val out: Outlet[Status[T]] = Outlet("output")
  override val shape: FlowShape[Record[T], Status[T]] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes) = {
    new GraphStageLogic(shape) with StageLogging {
      val buffer = new FileBuffer[T](table, format, maxRowsInBuffer)
      import system.dispatcher

      override def preStart(): Unit = {
        setKeepGoing(true)
      }

      override def postStop(): Unit = {
        super.postStop()
        logger.info("stage stopped")
      }
      val pullCallback = getAsyncCallback[Try[Done]] {
        case Success(_) =>
          logger.debug("flush complete")
          push(out, Status(buffer.passThrough))
          buffer.reset
        case Failure(ex) =>
          logger.error("cannot flush", ex)
          buffer.close
          failStage(ex)
      }

      val finishCallbach = getAsyncCallback[Try[Done]] {
        case Success(_) =>
          logger.debug("last flush done, completing")
          push(out, Status(buffer.passThrough))
          completeStage()
          buffer.close
        case Failure(ex) =>
          logger.error("cannot do last flush", ex)
          buffer.close
          failStage(ex)
      }

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          logger.debug("onPull: pull(in)")
          pull(in)
        }
      })

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val items = grab(in)
          buffer.append(items.row, items.passThrough)
          if (buffer.isFull) {
            logger.debug(s"buffer is full (rows = ${buffer.size}), flushing")
            flush(buffer.stream).onComplete(pullCallback.invoke)
          } else {
            //logger.debug("onPush: pull(in)")
            pull(in)
          }

        }

        override def onUpstreamFinish(): Unit = {
          logger.debug(s"upstream done, last flush (rows = ${buffer.size})")
          flush(buffer.stream).onComplete(finishCallbach.invoke)
        }
      })

    }
  }
}

object ClickhouseFlow {
  case class Record[T](row: Row, passThrough: T)
  case class Status[T](passThrough: Seq[T])
}