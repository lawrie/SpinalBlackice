package mylib

import spinal.core._

class FallingEdgeDetector extends Component {
  val io = new Bundle {
    val inS = in Bool
    val outS = out Bool
  }

  val inQ = Reg(Bool)

  inQ := io.inS
  io.outS := inQ && !io.inS;
}

object FallingEdgeDetector {
  def main(args: Array[String]) {
    SpinalVerilog(new FallingEdgeDetector)
  }
}

