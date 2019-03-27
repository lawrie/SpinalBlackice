package mylib

import spinal.core._

case class RisingEdgeDetector() extends Component {
  val io = new Bundle {
    val pin_in = in Bool
    val pin_out = out Bool
  }.setName("")

  val clk = Bool
  val clkDomain = ClockDomain(clk)

  io.pin_out := io.pin_in.rise;
}

object RisingEdgeDetector {
  def main(args: Array[String]) {
    SpinalVerilog(ClockDomain.external("pin", withReset = false)(RisingEdgeDetector()))
  }
}

