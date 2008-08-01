package fitnesse.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.io.IOException;

/**
 * Task to run fit tests. This task runs fitnesse tests and publishes the results.
 * <p/>
 * <pre>
 * Usage:
 * &lt;taskdef name=&quot;execute-fitnesse-tests&quot;
 *     classname=&quot;fitnesse.ant.ExecuteFitnesseTestsTask&quot;
 *     classpathref=&quot;classpath&quot; /&gt;
 * OR
 * &lt;taskdef classpathref=&quot;classpath&quot;
 *             resource=&quot;tasks.properties&quot; /&gt;
 * <p/>
 * &lt;execute-fitnesse-tests
 *     suitepage=&quot;FitNesse.SuiteAcceptanceTests&quot;
 *     fitnesseport=&quot;8082&quot;
 *     resultsdir=&quot;${results.dir}&quot;
 *     resultshtmlpage=&quot;fit-results.html&quot;
 *     classpathref=&quot;classpath&quot; /&gt;
 * </pre>
 */
public class ExecuteFitnesseTestsTask extends Task
{
	private String fitnesseHost = "localhost";
	private int fitnessePort;
	private String suitePage;
	private String resultsDir = ".";
	private String resultsHTMLPage;
	private String resultsXMLPage;
	private boolean debug = true;
	private boolean verbose = true;
	private boolean failOnError = true;
	private String testRunnerClass = "fitnesse.runner.TestRunner";
	private Path classpath;
	private String resultProperty;

	@Override
	public void execute() throws BuildException
	{
		try
		{
			int exitCode = executeRunnerClassAsForked();
			if(exitCode != 0)
			{
				log("Finished executing FitNesse tests: " + exitCode + " failures/exceptions");
				if(failOnError)
				{
					throw new BuildException(exitCode + " FitNesse test failures/exceptions");
				}
				else
				{
					getProject().setNewProperty(resultProperty, String.valueOf(exitCode));
				}
			}
			else
			{
				log("Fitnesse Tests executed successfully");
			}
		}
		catch(Exception e)
		{
			if(failOnError)
			{
				throw new BuildException(
					"Got an unexpected error trying to run the fitnesse tests : " + e.getMessage(), e);
			}
			else
			{
				e.printStackTrace();
			}
		}
	}

	private int executeRunnerClassAsForked() throws BuildException
	{
		CommandlineJava cmd = initializeJavaCommand();

		Execute execute = new Execute(new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN));
		execute.setCommandline(cmd.getCommandline());
		execute.setNewenvironment(false);
		execute.setAntRun(getProject());

		log(cmd.describeCommand(), Project.MSG_VERBOSE);
		int retVal;
		try
		{
			retVal = execute.execute();
		}
		catch(IOException e)
		{
			throw new BuildException("Process fork failed.", e, getLocation());
		}

		return retVal;
	}

	private CommandlineJava initializeJavaCommand()
	{
		CommandlineJava cmd = new CommandlineJava();
		cmd.setClassname(testRunnerClass);
		if(debug)
			cmd.createArgument().setValue("-debug");
		if(verbose)
			cmd.createArgument().setValue("-v");
		if(resultsHTMLPage != null)
		{
			String resultsHTMLPagePath = new File(resultsDir, resultsHTMLPage).getAbsolutePath();
			cmd.createArgument().setValue("-html");
			cmd.createArgument().setValue(resultsHTMLPagePath);
		}
		if(resultsXMLPage != null)
		{
			String resultsHTMLPagePath = new File(resultsDir, resultsXMLPage).getAbsolutePath();
			cmd.createArgument().setValue("-xml");
			cmd.createArgument().setValue(resultsHTMLPagePath);
		}
		cmd.createArgument().setValue("-nopath");
		cmd.createArgument().setValue(fitnesseHost);
		cmd.createArgument().setValue(String.valueOf(fitnessePort));
		cmd.createArgument().setValue(suitePage);
		cmd.createClasspath(getProject()).createPath().append(classpath);
		return cmd;
	}

	/**
	 * Host address on which Fitnesse is running. Defaults to 'localhost'.
	 *
	 * @param fitnesseHost
	 */
	public void setFitnesseHost(String fitnesseHost)
	{
		this.fitnesseHost = fitnesseHost;
	}

	/**
	 * Classpath of the TestRunner class. <b>MUST SET</b>
	 *
	 * @param classpath
	 */
	public void setClasspath(Path classpath)
	{
		this.classpath = classpath;
	}

	/**
	 * Debug mode. Defaults to 'true'.
	 *
	 * @param debug
	 */
	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	/**
	 * Will fail the build if any Fitnesse tests fail. Defaults to 'true'.
	 *
	 * @param failOnError
	 */
	public void setFailOnError(boolean failOnError)
	{
		this.failOnError = failOnError;
	}

	/**
	 * Port on which fitnesse would run. <b>MUST SET.</b>.
	 *
	 * @param fitnessePort
	 */
	public void setFitnessePort(int fitnessePort)
	{
		this.fitnessePort = fitnessePort;
	}

	/**
	 * Name of the property which will store the test results. Only valid if failOnError attribute is set to false.
	 *
	 * @param resultProperty
	 */
	public void setResultProperty(String resultProperty)
	{
		this.resultProperty = resultProperty;
	}

	/**
	 * Path to the folder that will contain the fitnesse results page after execution. Only valid if resultsHTMLPage or
	 * resultsXMLPage attributes are set. Defaults to current directory.
	 *
	 * @param resultsDir
	 */
	public void setResultsDir(String resultsDir)
	{
		this.resultsDir = resultsDir;
	}

	/**
	 * If set, stores the fitnesse results in HTML format under the resultsdir folder with the given name. The file name
	 * must have a '.html' extension.
	 *
	 * @param resultsHTMLPage
	 */
	public void setResultsHTMLPage(String resultsHTMLPage)
	{
		this.resultsHTMLPage = resultsHTMLPage;
	}

	/**
	 * If set, stores the fitnesse results in XML format under the resultsdir folder with the given name. The file name
	 * must have a '.xml' extension.
	 *
	 * @param resultsXMLPage
	 */
	public void setResultsXMLPage(String resultsXMLPage)
	{
		this.resultsXMLPage = resultsXMLPage;
	}

	/**
	 * Fully qualifies class name of the fitnesse testrunner class. Defaults to 'fitnesse.runner.TestRunner'.
	 *
	 * @param runnerClass
	 */
	public void setTestRunnerClass(String runnerClass)
	{
		this.testRunnerClass = runnerClass;
	}

	/**
	 * Partial URL of the wiki page which is declared as a Suite. Ex: FrontPage.SmokeTest,
	 * FitNesse.SuiteAcceptanceTests, or FitNesse.AcceptanceTestsSuite. <b>MUST SET.</b>
	 *
	 * @param suitePage
	 */
	public void setSuitePage(String suitePage)
	{
		this.suitePage = suitePage;
	}

	/**
	 * Set verbose mode. Defaults to 'true'.
	 *
	 * @param verbose
	 */
	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}

	public Path createClasspath()
	{
		if(classpath == null)
		{
			classpath = new Path(getProject());
		}
		return classpath.createPath();
	}

	public void setClasspathRef(Reference r)
	{
		createClasspath().setRefid(r);
	}
}
