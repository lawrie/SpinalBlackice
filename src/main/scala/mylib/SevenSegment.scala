package mylib

import spinal.core._

class SevenSegment extends Component {
  val io = new Bundle {
    val seg = out Bits(7 bits)
    val d = out Bool
    val switchs = in UInt(8 bits)
    val leds = out UInt(8 bits)
  }

  val segROM = Vec(Bits(7 bits), 16)

  segROM(0x0) := "1111110"
  segROM(0x1) := "0110000"
  segROM(0x2) := "1101101"
  segROM(0x3) := "1111001"
  segROM(0x4) := "0110011"
  segROM(0x5) := "1011011"
  segROM(0x6) := "1011111"
  segROM(0x7) := "1110000"
  segROM(0x8) := "1111111"
  segROM(0x9) := "1111011"
  segROM(0xa) := "1110111"
  segROM(0xb) := "0011111"
  segROM(0xc) := "1101110"
  segROM(0xd) := "0111101"
  segROM(0xe) := "1001111"
  segROM(0xf) := "1000111"

  val prescaler = Reg(UInt(24 bits))
  val digPos = Reg(Bool)
  val segOut = Reg(Bits(7 bits))

  io.seg := segOut
  io.d := digPos
  io.leds := io.switchs

  prescaler := prescaler + 1

  when (prescaler === 50000) {
    prescaler := 0;
    digPos := !digPos
   
    segOut := segROM(digPos ? io.switchs(3 downto 0) | io.switchs(7 downto 4)) 
  }
}

//Define a custom SpinalHDL configuration with boot reset instead of the default asynchronous one. This configuration can be resued everywhere
object BlackIceSpinalConfig extends SpinalConfig(defaultConfigForClockDomains = ClockDomainConfig(resetKind = BOOT))

object SevenSegment {
  def main(args: Array[String]) {
    BlackIceSpinalConfig.generateVerilog(new SevenSegment)
  }
}

