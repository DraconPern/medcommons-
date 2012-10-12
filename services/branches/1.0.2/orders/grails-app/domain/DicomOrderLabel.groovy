class DicomOrderLabel {
  
    static constraints = {
      label00(nullable: true)
      label01(nullable: true)
      label02(nullable: true)
      label03(nullable: true)
      label04(nullable: true)
      label05(nullable: true)
      label06(nullable: true)
      label07(nullable: true)
      label08(nullable: true)
      label09(nullable: true)
    }

    static mapping = {
      columns {
		label00 column:'label_00'
		label01 column:'label_01'
		label02 column:'label_02'
		label03 column:'label_03'
		label04 column:'label_04'
		label05 column:'label_05'
		label06 column:'label_06'
		label07 column:'label_07'
		label08 column:'label_08'
		label09 column:'label_09'
      }
    }

    DicomOrder dicomOrder
    String label00
    String label01
    String label02
    String label03
    String label04
    String label05
    String label06
    String label07
    String label08
    String label09
}
