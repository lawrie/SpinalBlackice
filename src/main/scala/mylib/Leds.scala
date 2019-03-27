package mylib

import spinal.core._

class Leds extends Component {
  val io = new Bundle {
    val leds = out UInt(4 bits)
  }

  io.leds <> 0xF
}

object Leds {
  def main(args: Array[String]) {
    SpinalVerilog(new Leds)
  }
}

