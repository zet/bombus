/*
 * LoginListener.java
 *
 * Created on 8 Июль 2006 г., 22:40
 *
 * Copyright (c) 2005-2006, Eugene Stahov (evgs), http://bombus.jrudevels.org
 * All rights reserved.
 */

package login;

/**
 *
 * @author evgs
 */
public interface LoginListener {
    public void loginFailed(String error);
    public void loginSuccess();
}
