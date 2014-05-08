package welc.dependencies.csvconverter;

public class OperationInfo {

	int idxOfFirstColumn;
	int idxOfSecondColumn; 
	int operationCode = 0; // 0 - nothing, 1 - concatenate columns and store under idxOfFirstColumn, 2 - swap, 3 - skip idxOfFirstColumn column
	String separatorRepleacement;

	public int getOperationCode() {
		return operationCode;
	}

	public int getIdxOfFirstColumn() {
		return idxOfFirstColumn;
	}

	public int getIdxOfSecondColumn() {
		return idxOfSecondColumn;
	}

	public String getSeparatorRepleacement() {
		return separatorRepleacement;
	}

	public OperationInfo(int operationCode, int idxOfFirstColumn, int idxOfSecondColumn, String separatorRepleacement) {
		this.operationCode = operationCode;
		this.idxOfFirstColumn = idxOfFirstColumn;
		this.idxOfSecondColumn = idxOfSecondColumn; 
		this.separatorRepleacement = separatorRepleacement;
	}

}
