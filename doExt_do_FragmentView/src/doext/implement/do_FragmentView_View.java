package doext.implement;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.helper.DoScriptEngineHelper;
import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.interfaces.DoIListData;
import core.interfaces.DoIPage;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoIUIModuleView;
import core.object.DoInvokeResult;
import core.object.DoMultitonModule;
import core.object.DoSourceFile;
import core.object.DoUIModule;
import doext.define.do_FragmentView_IMethod;
import doext.define.do_FragmentView_MAbstract;

/**
 * 自定义扩展UIView组件实现类，此类必须继承相应VIEW类，并实现DoIUIModuleView,do_FragmentView_IMethod接口；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.model.getUniqueKey());
 */
public class do_FragmentView_View extends DrawerLayout implements DoIUIModuleView, do_FragmentView_IMethod {

	/**
	 * 每个UIview都会引用一个具体的model实例；
	 */
	private do_FragmentView_MAbstract model;
	private String[] templates;
	private List<String> uiTemplatePath = new LinkedList<String>();
	private DoIListData _data;
	private boolean allowAnimation;

	public do_FragmentView_View(Context context) {
		super(context);
		// 设置左右滑动背景渐变阴影效果为透明，默认为灰色；
		// this.setScrimColor(Color.TRANSPARENT);
		initEvents();
	}

	/**
	 * 初始化加载view准备,_doUIModule是对应当前UIView的model实例
	 */
	@Override
	public void loadView(DoUIModule _doUIModule) throws Exception {
		this.model = (do_FragmentView_MAbstract) _doUIModule;
	}

	/**
	 * 动态修改属性值时会被调用，方法返回值为true表示赋值有效，并执行onPropertiesChanged，否则不进行赋值；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) {
		return true;
	}

	/**
	 * 属性赋值成功后被调用，可以根据组件定义相关属性值修改UIView可视化操作；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public void onPropertiesChanged(Map<String, String> _changedValues) {
		DoUIModuleHelper.handleBasicViewProperChanged(this.model, _changedValues);
		if (_changedValues.containsKey("templates")) {
			templates = _changedValues.get("templates").split(",");
		}
		if (_changedValues.containsKey("allowAnimation")) {
			allowAnimation = DoTextHelper.strToBool(_changedValues.get("allowAnimation"), false);
		}
		if (_changedValues.containsKey("supportGesture")) {
			// 支持 both 同时支持左右滑动(默认) , left 仅支持手势滑出左页,right 仅支持手势滑出右页
			setSupportGesture(_changedValues.get("supportGesture"));
		}
	}

	private void setSupportGesture(String _supportGesture) {
		if ("LEFT".equalsIgnoreCase(_supportGesture)) {
			setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
			setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
		} else if ("RIGHT".equalsIgnoreCase(_supportGesture)) {
			setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
			setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
		} else {
			setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {

		if ("showRight".equals(_methodName)) {
			showRight(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("showLeft".equals(_methodName)) {
			showLeft(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("bindItems".equals(_methodName)) {
			bindItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("refreshItems".equals(_methodName)) {
			refreshItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("reset".equals(_methodName)) {
			reset(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return false;
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.model.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		// ...do something
		return false;
	}

	/**
	 * 释放资源处理，前端JS脚本调用closePage或执行removeui时会被调用；
	 */
	@Override
	public void onDispose() {
		// ...do something
	}

	/**
	 * 重绘组件，构造组件时由系统框架自动调用；
	 * 或者由前端JS脚本调用组件onRedraw方法时被调用（注：通常是需要动态改变组件（X、Y、Width、Height）属性时手动调用）
	 */
	@Override
	public void onRedraw() {
		this.setLayoutParams(DoUIModuleHelper.getLayoutParams(this.model));
	}

	/**
	 * 获取当前model实例
	 */
	@Override
	public DoUIModule getModel() {
		return model;
	}

	/**
	 * 绑定视图模板数据；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void bindItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _address = DoJsonHelper.getString(_dictParas, "data", "");
		if (_address == null || _address.length() <= 0)
			throw new Exception("doFragmentView 未指定 data参数！");
		DoMultitonModule _multitonModule = DoScriptEngineHelper.parseMultitonModule(_scriptEngine, _address);
		if (_multitonModule == null)
			throw new Exception("doFragmentView data参数无效！");
		if (_multitonModule instanceof DoIListData) {
			initTemplates(templates);
			_data = (DoIListData) _multitonModule;
			initView(_scriptEngine);
		}
	}

	private void initView(DoIScriptEngine _scriptEngine) throws Exception {
		JSONObject childData = (JSONObject) _data.getData(0);
		int contentIndex = DoTextHelper.strToInt(DoJsonHelper.getString(childData, "template", "-1"), -1);
		if (contentIndex >= uiTemplatePath.size() || contentIndex < 0) {
			throw new Exception("doFragmentView template索引 " + contentIndex + "不存在！");
		}
		String contentTemplatePath = getTemplatePath(contentIndex);
		DoIUIModuleView contentModuleView = createModuleView(_scriptEngine.getCurrentPage(), contentTemplatePath);
		contentModuleView.getModel().setModelData(childData);
		this.addView((View) contentModuleView);// mainContentView
		createLeftFragmentView(childData, _scriptEngine.getCurrentPage());
		createRightFragmentView(childData, _scriptEngine.getCurrentPage());
	}

	private void createLeftFragmentView(JSONObject childData, DoIPage page) throws Exception {
		int leftIndex = DoTextHelper.strToInt(DoJsonHelper.getString(childData, "leftTemplate", "-1"), -1);
		String leftTemplatePath = getTemplatePath(leftIndex);
		if (!"".equals(leftTemplatePath)) {
			DoIUIModuleView leftModuleView = createModuleView(page, leftTemplatePath);
			leftModuleView.getModel().setModelData(childData);
			View leftView = (View) leftModuleView;
			DrawerLayout.LayoutParams leftLayoutParams = new DrawerLayout.LayoutParams((int) leftModuleView.getModel().getRealWidth(), (int) leftModuleView.getModel().getRealHeight());
			leftLayoutParams.gravity = Gravity.LEFT;
			leftView.setLayoutParams(leftLayoutParams);
			leftView.setTag("LEFT");
			this.addView(leftView);
		}
	}

	private void createRightFragmentView(JSONObject childData, DoIPage page) throws Exception {
		int rightIndex = DoTextHelper.strToInt(DoJsonHelper.getString(childData, "rightTemplate", "-1"), -1);
		String rightTemplatePath = getTemplatePath(rightIndex);
		if (!"".equals(rightTemplatePath)) {
			DoIUIModuleView rightModuleView = createModuleView(page, rightTemplatePath);
			rightModuleView.getModel().setModelData(childData);
			View rightView = (View) rightModuleView;
			DrawerLayout.LayoutParams rightLayoutParams = new DrawerLayout.LayoutParams((int) rightModuleView.getModel().getRealWidth(), (int) rightModuleView.getModel().getRealHeight());
			rightLayoutParams.gravity = Gravity.RIGHT;
			rightView.setLayoutParams(rightLayoutParams);
			rightView.setTag("RIGHT");
			this.addView(rightView);
		}
	}

	private String getTemplatePath(int index) {
		String path = "";
		try {
			return uiTemplatePath.get(index);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return path;
	}

	public void initTemplates(String[] templates) throws Exception {
		uiTemplatePath.clear();
		for (String templatePath : templates) {
			if (templatePath != null && !templatePath.equals("")) {
				DoSourceFile _sourceFile = model.getCurrentPage().getCurrentApp().getSourceFS().getSourceByFileName(templatePath);
				if (_sourceFile != null) {
					uiTemplatePath.add(templatePath);
				} else {
					throw new RuntimeException("试图使用一个无效的UI页面:" + templatePath);
				}
			}
		}
	}

	private DoIUIModuleView createModuleView(DoIPage page, String templatePath) throws Exception {
		DoUIModule module = DoServiceContainer.getUIModuleFactory().createUIModuleBySourceFile(templatePath, page, true);
		return module.getCurrentUIModuleView();
	}

	/**
	 * 刷新数据；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void refreshItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		removeAllViews();
		initView(_scriptEngine);
	}

	/**
	 * 显示左侧视图；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void showLeft(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		openDrawer(Gravity.LEFT);
	}

	/**
	 * 显示右侧视图；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void showRight(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		openDrawer(Gravity.RIGHT);
	}

	/**
	 * 重置为初始视图；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void reset(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		closeDrawers();
	}

	private void initEvents() {
		setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int newState) {

			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				if (!allowAnimation) {
					return;
				}
				View mContent = getChildAt(0);
				View mMenu = drawerView;
				float scale = 1 - slideOffset;
				float contentScale = 0.8f + scale * 0.2f;
				float menuScale = 1 - 0.3f * scale;
				float menuAlpha = 0.6f + 0.4f * (1 - scale);
				if (drawerView.getTag().equals("LEFT")) {
					ViewHelper.setScaleX(mMenu, menuScale);
					ViewHelper.setScaleY(mMenu, menuScale);
					ViewHelper.setAlpha(mMenu, menuAlpha);
					ViewHelper.setTranslationX(mContent, mMenu.getMeasuredWidth() * (1 - scale));
					ViewHelper.setPivotX(mContent, 0);
					ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
					mContent.invalidate();
					ViewHelper.setScaleX(mContent, contentScale);
					ViewHelper.setScaleY(mContent, contentScale);
				} else if (drawerView.getTag().equals("RIGHT")) {
					ViewHelper.setScaleX(mMenu, menuScale);
					ViewHelper.setScaleY(mMenu, menuScale);
					ViewHelper.setAlpha(mMenu, menuAlpha);
					ViewHelper.setTranslationX(mContent, -mMenu.getMeasuredWidth() * slideOffset);
					ViewHelper.setPivotX(mContent, mContent.getMeasuredWidth());
					ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
					mContent.invalidate();
					ViewHelper.setScaleX(mContent, contentScale);
					ViewHelper.setScaleY(mContent, contentScale);
				}
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				String aaaString = (String) drawerView.getTag();
				if ("LEFT".equals(aaaString)) {
					fireIndexChanged(1);
				} else {
					fireIndexChanged(2);
				}
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				fireIndexChanged(0);
			}
		});
	}

	//中间页面返回0,左边页面返回1,右边页面返回2
	private void fireIndexChanged(int result) {
		DoInvokeResult _invokeResult = new DoInvokeResult(this.model.getUniqueKey());
		_invokeResult.setResultInteger(result);
		this.model.getEventCenter().fireEvent("indexChanged", _invokeResult);
	}
}