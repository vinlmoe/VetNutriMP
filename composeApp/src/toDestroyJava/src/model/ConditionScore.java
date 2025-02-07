package model;

public class ConditionScore {
	private String nameScale="5";
			private float value=20;
			int  id=0;
			
			
			public ConditionScore(String ns, float v, int id) {
				// TODO Auto-generated constructor stub
				nameScale=ns;
				value=v;
				this.id=id;
				
			}
			public int getId() {
				return id;
			}
			public String getNameScale() {
				return nameScale;
			}
			public float getValue() {
				return value;
			}
			
			
			
	@Override
	public String  toString() {
		return nameScale;
	}
	
	

}
