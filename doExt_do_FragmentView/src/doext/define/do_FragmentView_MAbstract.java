package doext.define;

import core.object.DoUIModule;
import core.object.DoProperty;
import core.object.DoProperty.PropertyDataType;


public abstract class do_FragmentView_MAbstract extends DoUIModule{

	protected do_FragmentView_MAbstract() throws Exception {
		super();
	}
	
	/**
	 * 初始化
	 */
	@Override
	public void onInit() throws Exception {
        super.onInit();
        //注册属性
		this.registProperty(new DoProperty("templates", PropertyDataType.String, "", true));
		this.registProperty(new DoProperty("allowAnimation", PropertyDataType.Bool, "false", true));
		this.registProperty(new DoProperty("supportGesture", PropertyDataType.String, "", false));
	}
}