// Generator : SpinalHDL v1.3.1    git head : 9fe87c98746a5306cb1d5a828db7af3137723649
// Date      : 28/03/2019, 09:43:22
// Component : QspiTest


module QspiSlaveRX (
      input   io_qckRise,
      input   io_qssRise,
      input  [3:0] io_qd,
      output  io_rxReady,
      output [7:0] io_rxData,
      output [3:0] io_byteNumber,
      input   io_readEnable,
      input   clk,
      input   reset);
  reg [7:0] shiftReg;
  reg [7:0] lastByte;
  reg [3:0] byteNumber;
  reg  firstNibble;
  reg  rxReady;
  assign io_rxData = shiftReg;
  assign io_byteNumber = byteNumber;
  assign io_rxReady = rxReady;
  always @ (posedge clk) begin
    rxReady <= 1'b0;
    if(io_readEnable)begin
      if(io_qssRise)begin
        firstNibble <= 1'b1;
        lastByte <= shiftReg;
      end
      if(io_qckRise)begin
        if(firstNibble)begin
          byteNumber <= (byteNumber + (4'b0001));
          shiftReg[7 : 4] <= io_qd;
          firstNibble <= 1'b0;
        end else begin
          shiftReg[3 : 0] <= io_qd;
          rxReady <= 1'b1;
        end
      end
    end
  end

endmodule

module QspiSlaveTX (
      input   io_qckFall,
      input   io_qssRise,
      output [3:0] io_qd,
      output  io_txReady,
      input  [7:0] io_txData,
      input   io_writeEnable,
      input   clk,
      input   reset);
  reg [3:0] outData;
  reg  firstNibble;
  reg  txReady;
  assign io_qd = outData;
  assign io_txReady = txReady;
  always @ (posedge clk) begin
    if(io_writeEnable)begin
      if(io_qssRise)begin
        firstNibble <= 1'b1;
      end
      if(io_qckFall)begin
        if(firstNibble)begin
          outData <= io_txData[7 : 4];
          firstNibble <= 1'b0;
        end else begin
          outData <= io_txData[3 : 0];
          txReady <= 1'b1;
        end
      end
    end
  end

endmodule

module QspiTest (
      input   io_qck,
      input   io_qss,
      input  [3:0] io_qd_read,
      output [3:0] io_qd_write,
      output [3:0] io_qd_writeEnable,
      output [11:0] io_leds,
      input   clk,
      input   reset);
  wire [3:0] _zz_1_;
  wire  _zz_2_;
  wire  _zz_3_;
  wire  rx_io_rxReady;
  wire [7:0] rx_io_rxData;
  wire [3:0] rx_io_byteNumber;
  wire [3:0] tx_io_qd;
  wire  tx_io_txReady;
  reg [2:0] qssR;
  reg [7:0] qdR;
  reg [2:0] qckR;
  wire  qckRise;
  wire  qckFall;
  wire  qssRise;
  reg [3:0] readEnable;
  reg [7:0] rxData;
  reg [11:0] leds;
  QspiSlaveRX rx ( 
    .io_qckRise(qckRise),
    .io_qssRise(qssRise),
    .io_qd(_zz_1_),
    .io_rxReady(rx_io_rxReady),
    .io_rxData(rx_io_rxData),
    .io_byteNumber(rx_io_byteNumber),
    .io_readEnable(_zz_2_),
    .clk(clk),
    .reset(reset) 
  );
  QspiSlaveTX tx ( 
    .io_qckFall(qckFall),
    .io_qssRise(qssRise),
    .io_qd(tx_io_qd),
    .io_txReady(tx_io_txReady),
    .io_txData(rxData),
    .io_writeEnable(_zz_3_),
    .clk(clk),
    .reset(reset) 
  );
  assign qckRise = (qckR[2 : 1] == (2'b01));
  assign qckFall = (qckR[2 : 1] == (2'b10));
  assign qssRise = (qssR[2 : 1] == (2'b01));
  assign _zz_1_ = qdR[7 : 4];
  assign io_qd_write = tx_io_qd;
  assign io_qd_writeEnable = (~ readEnable);
  assign _zz_2_ = (readEnable != (4'b0000));
  assign _zz_3_ = (readEnable == (4'b0000));
  assign io_leds = leds;
  always @ (posedge clk) begin
    qssR <= {qssR[1 : 0],io_qss};
    qdR <= {qdR[3 : 0],io_qd_read};
    qckR <= {qckR[1 : 0],io_qck};
    if(qssRise)begin
      readEnable <= (~ readEnable);
    end
    if(rx_io_rxReady)begin
      rxData <= rx_io_rxData;
    end
    if(rx_io_rxReady)begin
      if((rx_io_byteNumber == (4'b0010)))begin
        leds[7 : 0] <= rx_io_rxData;
      end
      if((rx_io_byteNumber == (4'b0011)))begin
        leds[9 : 8] <= rx_io_rxData[1 : 0];
      end
    end
    leds[11 : 10] <= (2'b00);
  end

endmodule

