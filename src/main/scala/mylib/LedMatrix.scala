package mylib

import spinal.core._
import spinal.lib._

import spinal.lib.graphic._
import scala.math._

case class Gamma(source_bits : Int, destination_bits : Int, gamma_factor : Int )
{
  val numberOfElements : Int = pow(2, source_bits).toInt

  def apply(input : UInt) : UInt = {
    input.muxList(for(index <- 0 until numberOfElements) yield {
      var result = UInt(destination_bits bits)
      var in_f = index.toFloat / (numberOfElements - 1)
      var gamma_f = pow(in_f, gamma_factor)
      var ov = gamma_f * (pow(2, destination_bits) - 1)
      result := ceil(ov).toInt
      (index, result)
    } )
  }
}

case class LedPixel() extends Bundle {
    val r = Bool
    val g = Bool
    val b = Bool

    def pwm(value: Rgb, counter: UInt, gamma: Gamma) ={
        r := gamma(value.r) > counter
        g := gamma(value.g) > counter
        b := gamma(value.b) > counter
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
                    colorDepth: Int = 4) extends Component {
    val io = new Bundle{
        val output = out(LedMatrix(log2Up(rows/2)))
        val pixels = slave Stream (Rgb(rgbConfig))
    }

    val rowCounter = Counter(rows/2)
    val columnCounter = Counter(columns)
    val colorCounter = Counter((colorDepth + 5) bits)
    val pixelClk = Reg(Bool)

    io.output.latch := False
    io.output.oe := False
    io.output.row := rowCounter.value.asBits
    io.output.clk := pixelClk

    val gamma = Gamma(colorDepth, colorDepth + 5, 5)

    io.output.rgb1.pwm(io.pixels,colorCounter, gamma)
    io.output.rgb2.pwm(io.pixels,colorCounter, gamma)

    pixelClk := !pixelClk

    io.pixels.ready := pixelClk

    when (!pixelClk) {
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

class LedMatrixTest(rows: Int = 32, columns: Int = 32, colorDepth: Int = 4) extends Component {
    val io = new Bundle{
        val output = out(LedMatrix(log2Up(rows/2)))
        val r = in Bool
        val g = in Bool
        val b = in Bool
    }

    val frequency = 2000

    val counter = Counter((rows/2)*columns*pow(2, colorDepth).toInt*frequency)
    val intensity = Counter(colorDepth bits)

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
