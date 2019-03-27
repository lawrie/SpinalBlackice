package mylib

import spinal.core._

class LedGlow extends Component {
  val io = new Bundle {
    val led = out Bool
  }

  val cnt = Reg(UInt(28 bits))
  val pwm = Reg(UInt(5 bits))

  cnt := cnt + 1

  val pwmInput = UInt(4 bits)

  when (cnt(27)) {
    pwmInput := cnt(26 downto 23)
  } otherwise {
    pwmInput := ~cnt(26 downto 23)
  }

  pwm := pwm(3 downto 0).resize(5) + pwmInput.resize(5)

  io.led := pwm(4)  
}

object LedGlow {
  def main(args: Array[String]) {
    SpinalVerilog(new LedGlow)
  }
}

