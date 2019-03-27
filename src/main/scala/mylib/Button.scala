package mylib

import spinal.core._

class Button extends Component {
  val io = new Bundle {
    val led = out Bool
    val button = in Bool
  }

  io.led <> ~io.button
}

object Button {
  def main(args: Array[String]) {
    SpinalVerilog(new Button)
  }
}

