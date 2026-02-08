package fzmm.zailer.me.mixin_interfaces;


// owo-lib won't let me in component TextAreaComponent constructor change the textColor,
// and then it is a private attribute of a class that extends...
public interface IMultiLineBoxColorFix {

    void fzmm$textColor(int value);

    void fzmm$cursorColor(int value);
}
