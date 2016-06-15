package akka.http.scaladsl.marshallers.sprayjson

import akka.http.scaladsl.marshalling.{Marshaller, _}
import akka.http.scaladsl.model.{HttpCharsets, MediaTypes}
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.unmarshalling.{Unmarshaller, _}
import akka.http.scaladsl.util.FastFuture
import akka.util.ByteString
import spray.json.{CompactPrinter, JsValue, JsonParser, JsonPrinter, ParserInput, PrettyPrinter, RootJsonFormat, RootJsonWriter, _}

import scala.concurrent.Future
import scala.language.implicitConversions

trait ExtendedSprayJsonSupport {
  implicit def sprayJsonUnmarshallerConverter[T](reader: RootJsonReader[T]): FromEntityUnmarshaller[T] =
    sprayJsonUnmarshaller(reader)
  implicit def sprayJsonUnmarshaller[T](implicit reader: RootJsonReader[T]): FromEntityUnmarshaller[T] =
    sprayJsValueUnmarshaller.map(jsonReader[T].read)
  implicit def sprayJsonByteStringUnmarshaller[T](implicit reader: RootJsonReader[T]): Unmarshaller[ByteString, T] =
    Unmarshaller.withMaterializer[ByteString, JsValue](_ ⇒ implicit mat ⇒ { bs ⇒
      FastFuture.successful(JsonParser(bs.toArray[Byte]))
    }).map(jsonReader[T].read)
  implicit def sprayJsValueUnmarshaller: FromEntityUnmarshaller[JsValue] =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(`application/json`).mapWithCharset { (data, charset) ⇒
      val input =
        if (charset == HttpCharsets.`UTF-8`) ParserInput(data.toArray)
        else ParserInput(data.decodeString(charset.nioCharset.name)) // FIXME: identify charset by instance, not by name!
      JsonParser(input)
    }

  implicit def sprayJsonMarshallerConverter[T](writer: RootJsonWriter[T])(implicit printer: JsonPrinter = PrettyPrinter): ToEntityMarshaller[T] =
    sprayJsonMarshaller[T](writer, printer)
  implicit def sprayJsonMarshaller[T](implicit writer: RootJsonWriter[T], printer: JsonPrinter = CompactPrinter): ToEntityMarshaller[T] =
    sprayJsValueMarshaller compose writer.write
  implicit def sprayJsValueMarshaller(implicit printer: JsonPrinter = PrettyPrinter): ToEntityMarshaller[JsValue] =
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(printer)
  implicit def sprayByteStringMarshaller[T](implicit writer: RootJsonFormat[T], printer: JsonPrinter = CompactPrinter): Marshaller[T, ByteString] =
    sprayJsValueMarshaller.map(s ⇒ ByteString(s.toString)) compose writer.write

  implicit def sprayByteStringUnmarshaller[T](implicit reader: RootJsonFormat[T]): Unmarshaller[String, T] =
    Unmarshaller[String, T] { implicit ec => s => 
      Future { JsonParser(ParserInput(s)).convertTo[T] } 
    }

}
