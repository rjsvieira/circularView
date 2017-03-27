package rjsv.circularview.utils;

import android.view.animation.Animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Description
 *
 * @author <a href="mailto:ricardo.vieira@xpand-it.com">RJSV</a>
 * @version $Revision : 1 $
 */

public class CircleViewAnimationListener implements Animation.AnimationListener {

    private List<Animation.AnimationListener> listeners;

    public CircleViewAnimationListener() {
        this.listeners = new ArrayList<>();
    }

    public void registerAnimationListener(Animation.AnimationListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        } else {
            this.unregisterAnimationListeners();
        }
    }

    public void unregisterAnimationListeners() {
        this.listeners.clear();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        for (Animation.AnimationListener listener : listeners) {
            listener.onAnimationStart(animation);
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        for (Animation.AnimationListener listener : listeners) {
            listener.onAnimationEnd(animation);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        for (Animation.AnimationListener listener : listeners) {
            listener.onAnimationRepeat(animation);
        }
    }

}
