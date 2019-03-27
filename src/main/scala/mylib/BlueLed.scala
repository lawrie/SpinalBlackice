package mylib

import spinal.core._

class BlueLed extends Component {
  val io = new Bundle {
    val blueLed = out Bool
  }

  io.blueLed <> True
}

object BlueLed {
  def main(args: Array[String]) {
    SpinalVerilog(new BlueLed)
  }
}

