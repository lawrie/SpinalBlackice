module qspitop (
  input   CLK100,
  input  QSS,
  input  QCK,
  inout [3:0] QD,
  output [11:0] LEDS   
);
  wire [3:0] io_qd_read, io_qd_write, io_qd_writeEnable;
  
  SB_IO #(
    .PIN_TYPE(6'b 1010_01),
    .PULLUP(1'b0)
  ) qd [3:0] (
    .PACKAGE_PIN(QD),
    .OUTPUT_ENABLE(io_qd_writeEnable),
    .D_OUT_0(io_qd_write),
    .D_IN_0(io_qd_read)
  );

  QspiTest test (
    .clk(CLK100),
    .reset(0),
    .io_qss(QSS),
    .io_qck(QCK),
    .io_qd_read(io_qd_read),
    .io_qd_write(io_qd_write),
    .io_qd_writeEnable(io_qd_writeEnable),
    .io_leds(LEDS)
  );

endmodule
