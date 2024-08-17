package visualization;

import gui.shotinput.IClickListener;

import java.util.ArrayList;

public interface IInput {

    UpdateLoop getUpdateLoop();

    ArrayList<IClickListener> getClickListener();
}
