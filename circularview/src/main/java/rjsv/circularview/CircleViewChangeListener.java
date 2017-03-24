package rjsv.circularview;

public interface CircleViewChangeListener {

    void onPointsChanged(CircleView circleView, float points);

    void onStartTracking(CircleView circleView);

    void onStopTracking(CircleView circleView);

}
