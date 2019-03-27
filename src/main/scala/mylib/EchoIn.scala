package mylib

import spinal.core._
import spinal.lib._

class EchoIn extends Component {
  val io = new Bundle {
    val pin = in Bool
    val pulseLength = out UInt(8 bits)
    val ready = out Bool
    val req = in Bool
    val value = in Bool
  }

  val clockMhz = 100
  val micros = Reg(UInt(16 bits))
  val cms = Reg(UInt(8 bits))
  val counter = Reg(UInt(8 bits))

  io.ready := False
  io.pulseLength := 0
  
  when (io.req) {
    when (io.pin === io.value || cms === 255) {
      io.pulseLength := cms
      io.ready := True
    } otherwise {
      counter := counter + 1
  
      when (counter === (clockMhz - 1)) {
        micros := micros + 1
        counter := 0
      }

      when (micros === 59) { // (Speed of light in cm/us * 2) - 1
        cms := cms + 1
        micros := 0
      }
    }
  } otherwise {
    counter := 0
    micros := 0
    cms := 0
  }
}

class EchoTest extends Component {
  val io = new Bundle {
    val echo = in Bool
    val trigger = out Bool
    val leds = out UInt(8 bits) 
    val button = in Bool
    val seg = out Bits(7 bits)
    val d = out Bool
  }

  val req = Reg(Bool)
  val waitForEcho = Reg(Bool)
  val triggering = Reg(Bool)
  val counter = Reg(UInt(10 bits))
  val lastPulse = Reg(UInt(8 bits))
  io.leds := lastPulse

  val debounce = new Debounce
  debounce.io.pb := io.button
  io.trigger := triggering

  counter := counter + 1
 
  when (debounce.io.pbDown) {
    counter := 0
    lastPulse := 0
    triggering := True
  }

  when (triggering && counter === 1000) { // 10 microseconds && echo high
    waitForEcho := True
    triggering := False
  }    

  when (waitForEcho && io.echo) {
    req := True
    waitForEcho := False
  }
  
  val echoIn = new EchoIn
  echoIn.io.pin := io.echo
  echoIn.io.req := req
  echoIn.io.value := False

  when (req && echoIn.io.ready) {
    lastPulse := echoIn.io.pulseLength
    req := False
  }

  val seven = new SevenSegment
  io.seg := seven.io.seg
  io.d := seven.io.d
  seven.io.switchs := lastPulse
}

object EchoTest {
  def main(args: Array[String]) {
    BlackIceSpinalConfig.generateVerilog(new EchoTest)
  }
}
