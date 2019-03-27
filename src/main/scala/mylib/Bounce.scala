package mylib

import spinal.core._
import spinal.lib._

class Bounce extends Component {
  val io = new Bundle {
    val leds = out(Reg(UInt(4 bits)))
    val button = in Bool
  }

  when (!io.button) {
    io.leds := io.leds + 1
  }
}

object Bounce {
  def main(args: Array[String]) {
    SpinalVerilog(new Bounce)
  }
}

