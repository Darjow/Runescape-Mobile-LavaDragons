package nodes;

import script.LavaDragon;

public abstract class Node {
	
	protected LavaDragon script;
	
	public Node(LavaDragon e) {
		script = e;
	}
	
	public abstract boolean validate();
	
	public abstract void execute();
	
	public  String status(){
		return this.getClass().getSimpleName();
	}
		

}
