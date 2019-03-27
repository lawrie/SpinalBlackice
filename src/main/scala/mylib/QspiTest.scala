package mylib

import spinal.core._
import spinal.lib._
import spinal.lib.io.TriStateArray
import spinal.lib.master

class QspiTest extends Component {
  val io = new Bundle {
    val qck = in Bool
    val qss = in Bool
    val qd = master(TriStateArray(4))
    val leds = out Bits(12 bits)
  }

  val qssR = Reg(Bits(3 bits))
  qssR := qssR(1 downto 0) ## io.qss

  val qdR = Reg(Bits(8 bits))
  qdR := qdR(3 downto 0) ## io.qd.read

  val qckR = Reg(Bits(3 bits))
  qckR := qckR(1 downto 0) ## io.qck

  val qckRise = (qckR(2 downto 1) === B"01")
  val qckFall = (qckR(2 downto 1) === B"10")
  val qssRise = (qssR(2 downto 1) === B"01")

  val rx = new QspiSlaveRX
  rx.io.qckRise := qckRise
  rx.io.qssRise := qssRise
  rx.io.qd :=  qdR(7 downto 4)
  
  val tx = new QspiSlaveTX
  tx.io.qckFall := qckFall
  tx.io.qssRise := qssRise
  io.qd.write := tx.io.qd
  
  val readEnable = Reg(Bits(4 bits))
  io.qd.writeEnable := ~readEnable

  when (qssRise) { // deselect io.qss
    readEnable := ~readEnable
  }

  // Led diagnostics  
  val leds = Reg(Bits(12 bits))
  io.leds := leds

  when (rx.io.rxReady) {  
    leds(7 downto 0) := rx.io.rxData
  }

  leds(8) := io.qd.read(0)
  leds(9) := io.qd.read(1)
  leds(10) := io.qck
  leds(11) := io.qss
  
  tx.io.txData := leds(7 downto 0)
}
  
class QspiSlaveTX extends Component {
  val io = new Bundle {
    val qckFall = in Bool
    val qssRise = in Bool
    val qd = out Bits(4 bits)
    val txReady = out Bool
    val txData = in Bits(8 bits)
  }

  val outData = Reg(Bits(4 bits))
  val firstNibble = Reg(Bool)

  io.qd := outData

  val txReady = Reg(Bool)  
  io.txReady := txReady

  when (io.qssRise) { // io.qss deselect
    firstNibble := True
  } 

  when (io.qckFall) {
    when (firstNibble) {
      outData := io.txData(7 downto 4)
      firstNibble := False
    } otherwise {
      outData := io.txData(3 downto 0)
      txReady := True
    }
  }    
}

class QspiSlaveRX extends Component {
  val io = new Bundle {
    val qckRise = in Bool
    val qssRise = in Bool
    val qd = in Bits(4 bits)
    val rxReady = out Bool
    val rxData = out Bits(8 bits)
  }
  
  val shiftReg = Reg(Bits(8 bits))
  io.rxData := shiftReg
  
  val firstNibble = Reg(Bool)

  when (io.qssRise) {
    firstNibble := True
  } 

  val rxReady = Reg(Bool) 
  io.rxReady := rxReady

  rxReady := False

  when (io.qckRise) { 
    when (firstNibble) {
      shiftReg(7 downto 4) := io.qd
      firstNibble := False
    } otherwise {
      shiftReg(3 downto 0) := io.qd
      rxReady := True
    }
  }    
}

object QspiTest {
  def main(args: Array[String]) {
    SpinalVerilog(new QspiTest)
  }
}

