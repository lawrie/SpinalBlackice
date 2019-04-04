package mylib

import spinal.core._

class PWM(width: Int) extends Component {
  val io = new Bundle {
    val pins = out Bits(width bits)
    val duty = in Vec(UInt(8 bits), width)
  }

  for(i <- 0 until width) {
    val counter = Reg(UInt(8 bits))

    counter := counter + 1

    io.pins(i) := counter <= io.duty(i) && io.duty(i) =/= 0
  }
}

class PWMTest(width: Int) extends Component {
  val io = new Bundle {
    val pins = out Bits(width bits)
  }

  val pwm = new PWM(width)
  pwm.io.pins <> io.pins

  for (i <- 0 until width) {
    pwm.io.duty(i) := i * 4
  }
}

object PWMTest {
  def main(args: Array[String]) {
    SpinalVerilog(new PWMTest(8))
  }
}

