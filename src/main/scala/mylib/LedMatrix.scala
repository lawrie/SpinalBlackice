package mylib

import spinal.core._
import spinal.lib._

import spinal.lib.graphic._
import scala.math._

case class LedPixel() extends Bundle {
    val r = Bool
    val g = Bool
    val b = Bool

    def pwm(value: Rgb, counter: UInt) ={
        r := value.r > counter
        g := value.g > counter
        b := value.b > counter
    }
}

case class LedMatrix(rowBits: Int) extends Bundle {
    val rgb1 = LedPixel()
    val rgb2 = LedPixel()
    val row = Bits(rowBits bits)
    val latch = Bool
    val oe = Bool
    val clk = Bool
}

class LedMatrixCtrl(rows: Int = 32, columns: Int = 32, 
                    rgbConfig : RgbConfig = RgbConfig(4,4,4), 
                    colorDepth: Int = 16) extends Component {
    val io = new Bundle{
        val output = out(LedMatrix(log2Up(rows/2)))
        val pixels = slave Stream (Rgb(rgbConfig))
    }

    val rowCounter = Counter(rows/2)
    val columnCounter = Counter(columns)
    val colorCounter = Counter(colorDepth)
    val state = Reg(Bool)

    io.output.latch := False
    io.output.oe := False
    io.output.row := rowCounter.value.asBits
    io.output.clk := state

    io.output.rgb1.pwm(io.pixels,colorCounter)
    io.output.rgb2.pwm(io.pixels,colorCounter)

    state := !state

    io.pixels.ready := state

    when (!state) {
        columnCounter.increment()
    }
  
    when(columnCounter.willOverflow) {
        colorCounter.increment()
        io.output.latch := True
        io.output.oe := True;
    }

    when(colorCounter.willOverflow) {
        rowCounter.increment()
    }
}

class LedMatrixTest(rows: Int = 32, columns: Int = 32, colorDepth: Int = 16) extends Component {
    val io = new Bundle{
        val output = out(LedMatrix(log2Up(rows/2)))
        val r = in Bool
        val g = in Bool
        val b = in Bool
    }

    val frequency = 1000

    val counter = Counter((rows/2)*columns*colorDepth*frequency)
    val intensity = Counter(colorDepth)

    val ledMatrixCtrl = new LedMatrixCtrl()
    ledMatrixCtrl.io.output <> io.output
    ledMatrixCtrl.io.pixels.r := io.r ? intensity.value | U(0)
    ledMatrixCtrl.io.pixels.g := io.g ? intensity.value | U(0) 
    ledMatrixCtrl.io.pixels.b := io.b ? intensity.value | U(0)     
    ledMatrixCtrl.io.pixels.valid := True
    
    when (ledMatrixCtrl.io.pixels.ready) {
        counter.increment()
    }

    when (counter.willOverflow) {
        intensity.increment();
    }
}

object LedMatrixTest {
    def main(args: Array[String]): Unit = {
        BlackIceSpinalConfig.generateVerilog(new LedMatrixTest())
    }
}
