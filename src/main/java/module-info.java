import com.lweyn.sparkreader.Main;

module Spark.Reader
{
    requires java.desktop;//swing UI
    requires log4j;//logging
    requires java.logging;
    requires jnativehook;//keyboard input
    requires jna;//memory hook prototype
    requires jna.platform;
    requires eb4j.core;//EPWING dictionary support
    requires commons.lang;//used in EPWING parsing at one point. FIXME can this be removed? Should look into this
    requires gson;//Used for VNDB API parsing
    exports com.lweyn.sparkreader.hooker;//needed for JNA to work

    opens com.lweyn.sparkreader;//needed to run Main
}