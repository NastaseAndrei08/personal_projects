library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity IDecode is
    Port ( clk: in STD_LOGIC;
           en : in STD_LOGIC;    
           Instr : in STD_LOGIC_VECTOR(12 downto 0);
           WD : in STD_LOGIC_VECTOR(15 downto 0);
           RegWrite : in STD_LOGIC;
           ExtOp : in STD_LOGIC;
           RD1 : out STD_LOGIC_VECTOR(15 downto 0);
           RD2 : out STD_LOGIC_VECTOR(15 downto 0);
           Ext_Imm : out STD_LOGIC_VECTOR(15 downto 0);
           func : out STD_LOGIC_VECTOR(2 downto 0);
           sa : out STD_LOGIC;
           WA: in std_logic_vector(2 downto 0);
           rd: out std_logic_vector(2 downto 0);
           rt: out std_logic_vector(2 downto 0)
           );
end IDecode;

architecture Behavioral of IDecode is

-- RegFile
type reg_array is array(0 to 7) of STD_LOGIC_VECTOR(15 downto 0);
signal reg_file : reg_array := (others => X"0000");

signal RegAddress: STD_LOGIC_VECTOR(2 downto 0);

begin

    sa <= Instr(3);
    func <= Instr(2 downto 0);
    
    rt <= Instr(9 downto 7);
    rd <= Instr(6 downto 4);
    
    RD1 <= reg_file(conv_integer(Instr(12 downto 10))); -- rs
    RD2 <= reg_file(conv_integer(Instr(9 downto 7))); -- rt
    
    Ext_Imm(6 downto 0) <= Instr(6 downto 0); 
    
    with ExtOp select
        Ext_Imm(15 downto 7) <= (others => Instr(6)) when '1',
                                (others => '0') when '0',
                                (others => '0') when others;

 
    process(clk)			
    begin
        if falling_edge(clk) then
            if en = '1' and RegWrite = '1' then
                reg_file(conv_integer(WA)) <= WD;		
            end if;
        end if;
    end process;		

end Behavioral;