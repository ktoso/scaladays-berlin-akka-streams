package scaladays.akka.kafka

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.ExtendedSprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka._
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.scaladsl.{Producer, _}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

import scaladays.akka.domain.Tweet
import scaladays.akka.http.MyJsonProtocol

case class KafkaTopics(system: ActorSystem) extends ExtendedSprayJsonSupport with MyJsonProtocol {

  import system.dispatcher

  val StatusTopicName = "status"
  val NamesTopicName = "names"

  private val bootstrapServers = system.settings.config.getString("kafka.bootstrap-servers")

  private val statusConsumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer,
    Set(StatusTopicName))
    .withBootstrapServers(bootstrapServers)
    .withGroupId("status-consumers")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    .withClientId("client-1")
  
  private val namesConsumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer,
    Set(NamesTopicName))
    .withBootstrapServers(bootstrapServers)
    .withGroupId("status-consumers")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    .withClientId("client-1")
  
  private val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)

  def statusSink: Sink[Tweet, NotUsed] =
    Flow[Tweet]
      .mapAsync(1) { s => Marshal(s).to[String] }
      .map(elem => new ProducerRecord[Array[Byte], String](StatusTopicName, elem))
      .to(Producer.plainSink(producerSettings))

  def statusSource(implicit mat: Materializer): Source[CommittableMessage[Array[Byte], Tweet], Control] =
    Consumer.committableSource(statusConsumerSettings)
      .mapAsync(1)(c => Unmarshal(c.value).to[Tweet].map(s => c.copy(value = s)))

  def namesSink: Sink[String, NotUsed] =
    Flow[String]
      .map(elem => new ProducerRecord[Array[Byte], String](NamesTopicName, elem))
      .to(Producer.plainSink(producerSettings))

  def namesSource(implicit mat: Materializer): Source[CommittableMessage[Array[Byte], String], Control] =
    Consumer.committableSource(namesConsumerSettings)

}
