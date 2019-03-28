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

  // Detect rise on qss and rise and fall on qck
  val qssR = Reg(Bits(3 bits))
  qssR := qssR(1 downto 0) ## io.qss

  val qdR = Reg(Bits(8 bits))
  qdR := qdR(3 downto 0) ## io.qd.read

  val qckR = Reg(Bits(3 bits))
  qckR := qckR(1 downto 0) ## io.qck

  val qckRise = (qckR(2 downto 1) === B"01")
  val qckFall = (qckR(2 downto 1) === B"10")
  val qssRise = (qssR(2 downto 1) === B"01")

  // QSPI RX slave
  val rx = new QspiSlaveRX
  rx.io.qckRise := qckRise
  rx.io.qssRise := qssRise
  rx.io.qd :=  qdR(7 downto 4)
  
  // QSPI TX slave
  val tx = new QspiSlaveTX
  tx.io.qckFall := qckFall
  tx.io.qssRise := qssRise
  io.qd.write := tx.io.qd
  
  // Swap between reading and writing when qss rises
  val readEnable = Reg(Bits(4 bits))
  io.qd.writeEnable := ~readEnable
  rx.io.readEnable := (readEnable =/= 0)
  tx.io.writeEnable := (readEnable === 0)

  when (qssRise) {
    readEnable := ~readEnable
  }

  // Read byte and echo it back
  val rxData = Reg(Bits(8 bits))

  when (rx.io.rxReady) {  
    rxData := rx.io.rxData
  }

  tx.io.txData := rxData

  // Show A0 10-bit analog value
  val leds = Reg(Bits(12 bits))
  io.leds := leds

  when (rx.io.rxReady) {
    when (rx.io.byteNumber === 2) {
      leds(7 downto 0) := rx.io.rxData
    }

    when (rx.io.byteNumber === 3) {
      leds(9 downto 8) := rx.io.rxData(1 downto 0)
    }
  }
  
  leds(11 downto 10) := 0
}
  
class QspiSlaveTX extends Component {
  val io = new Bundle {
    val qckFall = in Bool
    val qssRise = in Bool
    val qd = out Bits(4 bits)
    val txReady = out Bool
    val txData = in Bits(8 bits)
    val writeEnable = in Bool
  }

  val outData = Reg(Bits(4 bits))
  val firstNibble = Reg(Bool)

  io.qd := outData

  val txReady = Reg(Bool)  
  io.txReady := txReady

  when (io.writeEnable) {
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
}

class QspiSlaveRX extends Component {
  val io = new Bundle {
    val qckRise = in Bool
    val qssRise = in Bool
    val qd = in Bits(4 bits)
    val rxReady = out Bool
    val rxData = out Bits(8 bits)
    val byteNumber = out UInt(4 bits)
    val readEnable = in Bool
  }
  
  val shiftReg = Reg(Bits(8 bits))
  io.rxData := shiftReg

  val lastByte = Reg(Bits(8 bits))
  val byteNumber = Reg(UInt(4 bits))
  io.byteNumber := byteNumber

  val firstNibble = Reg(Bool)

  val rxReady = Reg(Bool) 
  io.rxReady := rxReady

  rxReady := False

  when (io.readEnable) {
    when (io.qssRise) {
      firstNibble := True
      lastByte := shiftReg
    } 

    when (io.qckRise) { 
      when (firstNibble) {
        byteNumber := byteNumber + 1
        shiftReg(7 downto 4) := io.qd
        firstNibble := False
      } otherwise {
        shiftReg(3 downto 0) := io.qd
        rxReady := True
      }
    }    
  }
}

object QspiTest {
  def main(args: Array[String]) {
    SpinalVerilog(new QspiTest)
  }
}

