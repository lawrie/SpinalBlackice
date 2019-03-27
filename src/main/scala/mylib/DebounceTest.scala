package mylib

import spinal.core._
import spinal.lib._

class DebounceTest extends Component {
  val io = new Bundle {
    val button = in Bool
    val leds = out(Reg(UInt(4 bits)))
    val blue_led = out(Bool)
    val green_led = out(Bool)
    val yellow_led = out(Bool)
    val red_led = out(Bool)
  }

  val debounce = new Debounce
  debounce.io.pb := io.button
  io.blue_led := !io.button
  io.green_led := False
  io.yellow_led := False
  io.red_led := False

  when (debounce.io.pbDown) {
    io.leds := io.leds + 1
  }
}

class Debounce extends Component {
  val io = new Bundle {
    val pb = in Bool
    val pbState = out(Reg(Bool))
    val pbDown = out Bool
    val pbUp = out Bool
  }

  val pbSync0 = Reg(Bool)
  val pbSync1 = Reg(Bool)

  pbSync0 := ~io.pb
  pbSync1 := pbSync0

  val pbIdle = (io.pbState === pbSync1)
  val pbCnt = Reg(UInt(16 bits))
  val pbCntMax = (pbCnt === 0xFFFF)
  
  io.pbDown <> (!pbIdle & pbCntMax & !io.pbState)
  io.pbUp <> (!pbIdle & pbCntMax & io.pbState)

  when (pbIdle) {
    pbCnt := 0;
  } otherwise {
   pbCnt := pbCnt + 1
   when (pbCntMax) {
     io.pbState := ~io.pbState
   }
  }
}

object DebounceTest {
  def main(args: Array[String]) {
    SpinalVerilog(new DebounceTest)
  }
}

