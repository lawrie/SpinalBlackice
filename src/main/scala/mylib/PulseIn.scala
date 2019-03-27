package mylib

import spinal.core._
import spinal.lib._

class PulseIn extends Component {
  val io = new Bundle {
    val pin = in Bool
    val pulseLength = out UInt(16 bits)
    val req = in Bool
    val value = in Bool
    val timeout = in UInt(16 bits)
    val ready = out Bool
  }

  val clockMhz = 100
  val micros = Reg(UInt(16 bits))
  val counter = Reg(UInt(8 bits))
  val req = Reg(Bool)

  io.pulseLength := 0
  io.ready := False

  when (io.req) {
    when (io.pin === io.value || (io.timeout > 0 && micros === (io.timeout - 1))) {
      io.pulseLength := micros
      io.ready := True
    } otherwise {
      counter := counter + 1
  
      when (counter === (clockMhz - 1)) {
        micros := micros + 1
        counter := 0
      }
    }
  } otherwise {
    counter := 0
    micros := 0
  }
}

class PulseTest extends Component {
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
  val lastPulse = Reg(UInt(16 bits))
  io.leds := lastPulse(7 downto 0)

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
  
  val pulseIn = new PulseIn
  pulseIn.io.pin := io.echo
  pulseIn.io.req := req
  pulseIn.io.value := False
  pulseIn.io.timeout := 0

  when (req && pulseIn.io.ready) {
    lastPulse := pulseIn.io.pulseLength
    req := False
  }

  val seven = new SevenSegment
  io.seg := seven.io.seg
  io.d := seven.io.d
  seven.io.switchs := lastPulse(15 downto 8)
}

object PulseTest {
  def main(args: Array[String]) {
    BlackIceSpinalConfig.generateVerilog(new PulseTest)
  }
}
