// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package communitycommons.actions;

import communitycommons.Logging;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;

/**
 * End timing something, and print the result to the log. 
 * - TimerName. Should correspond to the TimerName used with TimeMeasureStart.
 * - LogLevel. The loglevel used to print the result.
 * - The message to be printed in the log.
 */
public class TimeMeasureEnd extends CustomJavaAction<java.lang.Long>
{
	private java.lang.String TimerName;
	private communitycommons.proxies.LogLevel Loglevel;
	private java.lang.String message;

	public TimeMeasureEnd(IContext context, java.lang.String TimerName, java.lang.String Loglevel, java.lang.String message)
	{
		super(context);
		this.TimerName = TimerName;
		this.Loglevel = Loglevel == null ? null : communitycommons.proxies.LogLevel.valueOf(Loglevel);
		this.message = message;
	}

	@java.lang.Override
	public java.lang.Long executeAction() throws Exception
	{
		// BEGIN USER CODE
		return Logging.measureEnd(TimerName, Loglevel, message);
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "TimeMeasureEnd";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}