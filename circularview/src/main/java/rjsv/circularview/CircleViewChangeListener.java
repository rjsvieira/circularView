package rjsv.circularview;

public interface CircleViewChangeListener {

    void onPointsChanged(CircleView circleView, float points, boolean fromUser);

    void onStartTracking(CircleView circleView);

    void onStopTracking(CircleView circleView);

}
