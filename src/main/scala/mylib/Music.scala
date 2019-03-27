package mylib

import spinal.core._
import spinal.lib._

class Music extends Component {
  val io = new Bundle {
    val speaker = out Bool
  }

  val speakerOut = Reg(Bool)
  io.speaker := speakerOut

  val counter = Counter(100000000/440/2, True)

  when (counter === 0) {
    speakerOut := !speakerOut
  }
}

object Music {
  def main(args: Array[String]) {
    BlackIceSpinalConfig.generateVerilog(new Music)
  }
}
