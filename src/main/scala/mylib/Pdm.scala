package mylib

import spinal.core._
import spinal.lib._

class Pdm(width: Int) extends Component {
  val io = new Bundle {
    val din = in SInt(width bits)
    val dout = out Bool
    val accOut = out UInt(width + 1 bits)
  }

  val accumulator = Reg(UInt(width+1 bits))
  io.accOut := accumulator
  
  val unsignedDin = (io.din.asUInt ^ (1 << (width-1)))

  accumulator := accumulator(width-1 downto 0).resize(width+1) + unsignedDin.resize(width+1)

  io.dout := accumulator(width)
}

class ClkDivider(divisor: Int) extends Component {
  val io = new Bundle {
    val cout = out Bool
  }

  val counter = Reg(UInt(28 bits)) init 0

  val increment = (1 << 28) / divisor

  counter := counter + increment

  io.cout := counter(27)
}

class PdmTest(width: Int) extends Component {
  val io = new Bundle {
    val audio = out Bool
    val leds = out Bits(8 bits)
    val reset = in Bool
  }

  val clockHz = 100000000
  val bpm = 120
  val tickHz = ((bpm * 4) / 60)

  val oneMHzClk = new ClkDivider(clockHz / 1000000)
  val sampleClk = new ClkDivider(clockHz / 44100)
  val tickClk = new ClkDivider(clockHz / tickHz)

  val oneMHzDomain = new ClockDomain(clock=oneMHzClk.io.cout, reset=io.reset)
  val pdm = new Pdm(width)

  val oneMHzArea = new ClockingArea(oneMHzDomain) {
    val songPlayer = new SongPlayer(dataBits = 12)
    songPlayer.io.sampleClk := sampleClk.io.cout
    songPlayer.io.tickClk := tickClk.io.cout

    pdm.io.din := songPlayer.io.dout addTag(crossClockDomain)
  }

  io.leds := pdm.io.accOut(12 downto 5).asBits
  io.audio := pdm.io.dout
}

object PdmTest {
  def main(args: Array[String]) {
    SpinalVerilog(new PdmTest(12))
  }
}

