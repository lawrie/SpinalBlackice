package mylib

import spinal.core._
import spinal.lib.fsm._

class Sram extends Component {
  val io = new Bundle {
    val addr = out UInt(18 bits)
    val datIn = in UInt(16 bits)
    val datOut = out UInt(16 bits)
    val blueLed = out Bool
    val greenLed = out Bool
    val yellowLed = out Bool
    val redLed = out Bool
    val leds = out UInt(8 bits)
    val oe = out Bool
    val we = out Bool
    val cs = out Bool
    val ub = out Bool
    val lb = out Bool
    val button1 = in Bool
  }

  val redLed = Reg(Bool)

  io.blueLed := io.cs
  io.greenLed := io.oe
  io.redLed := redLed
  io.yellowLed := io.we

  val we = Reg(Bool) init True
  val oe = Reg(Bool) init True

  io.we := !we
  io.oe := !oe

  // Keep chip enabled and upper and lower bytes selected
  io.ub := False
  io.lb := False
  io.cs := False

  // Show least significant 8 bits on leds
  val datIn = Reg(UInt(16 bits))
  io.leds := datIn(7 downto 0)

  val addr = Reg(UInt(18 bits))
  io.addr := addr

  val datOut = Reg(UInt(16 bits))
  io.datOut := datOut

  val fsm = new StateMachine {

    val writing : State = new State with EntryPoint {
      whenIsActive {
        datOut := addr(15 downto 0) // set contents the same as address
        we := io.button1 // Suppress write if button pressed

        goto(writingEnd)
      }
    }

    val writingEnd : State = new State {
      whenIsActive {
        we := False // data written on rising edge
        goto(reading)
      }
    }

    val reading : State = new State {
      whenIsActive {
        when (addr > 0) { // Skip reading for address 0
          addr := addr - 1 // read previous
          oe := True
          goto(readDelay)
        } otherwise {
          addr := 1;
          goto(writing)
        }    
      }
    }

    val readDelay : State = new StateDelay(cyclesCount=100000000) {
      whenCompleted {
        datIn := io.datIn
        oe  := False

        // Set red led if mismatch
        redLed := (io.datIn =/= addr(15 downto 0))

        // Go on to next address
        addr := addr + 2
        goto(writing)
      }
    }
  }
}

object Sram {
  def main(args: Array[String]) {
    BlackIceSpinalConfig.generateVerilog(new Sram)
  }
}

