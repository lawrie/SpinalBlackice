package mylib

import spinal.core._

class PWM extends Component {
  val io = new Bundle {
    val pin = out Bool
    val duty = in UInt(8 bits)
  }

  val counter = Reg(UInt(8 bits))

  counter := counter + 1

  io.pin := counter <= io.duty && !(io.duty === 0)
}

class PWMTest extends Component {
  val io = new Bundle {
    val pin = out Bool
  }

  val pwm = new PWM
  pwm.io.pin <> io.pin
  pwm.io.duty := 127

}

object PWMTest {
  def main(args: Array[String]) {
    SpinalVerilog(new PWMTest)
  }
}

