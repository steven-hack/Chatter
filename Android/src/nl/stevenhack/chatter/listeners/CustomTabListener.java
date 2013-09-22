package nl.stevenhack.chatter.listeners;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * 
 * @author Steven
 *
 * This class handles the selection of the fragments.
 * 
 */
public class CustomTabListener<T extends Fragment> implements TabListener {

	private Fragment       mFragment;
    private final Activity mActivity;
    private final String   mTag;
    private final Class<T> mClass;

	public CustomTabListener(Activity activity, String tag, Class<T> clz) {
		mActivity = activity;
		mTag      = tag;
		mClass    = clz;
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (mFragment == null) {
			mFragment = Fragment.instantiate(mActivity, mClass.getName());
			ft.add(android.R.id.content, mFragment, mTag);
		}
		else {
			ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
			ft.attach(mFragment);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (mFragment != null) {
			ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
			ft.detach(mFragment);
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) { }
}