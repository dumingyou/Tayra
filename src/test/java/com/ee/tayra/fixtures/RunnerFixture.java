package com.ee.tayra.fixtures;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.ee.tayra.runner.Runner;

import fit.Parse;
import fit.exception.FitFailureException;
import fit.exception.MissingCellsFailureException;
import fitlibrary.DoFixture;
import groovy.lang.Binding;

public class RunnerFixture extends DoFixture {

  private static final String STDOUT = "stdout";
  private static final String STDERR = "stderr";
  private PrintStream stdout;
  private PrintStream stderr;
  private ByteArrayOutputStream err;
  private ByteArrayOutputStream out;

  public RunnerFixture() {
    out = new ByteArrayOutputStream();
    err = new ByteArrayOutputStream();
    stdout = new PrintStream(out, true);
    stderr = new PrintStream(err, true);
  }

  public final void run(final Parse cells) throws IOException {
    Parse args = cells.more;
    if (args == null) {
        throw new MissingCellsFailureException(cells.text()
        + " requires an argument");
    }
    Binding context = new Binding();
    context.setVariable("args", args.text().split(" "));
    System.setOut(stdout);
    System.setErr(stderr);
    try {
      new Runner(context).run();
    } catch (Throwable t) {
      StringBuilder message =
              new StringBuilder("Oops!! I cannot run any further...");
      if (t.getMessage() != null) {
        message.append(t.getMessage());
      }
      exception(args, message.toString());
    } finally {
      stdout.close();
      stderr.close();
    }
  }

  public final void show(final Parse cells) {
    Parse stream = cells.more;
    if (stream == null) {
        throw new MissingCellsFailureException(cells.text()
        + " requires an argument");
    }
    String consoleStream = stream.text();
    if (STDOUT.equalsIgnoreCase(consoleStream)) {
      stream.addToBody("<hr/>" + label("Console Output") + "<hr/>");
      stream.addToBody("<pre/>" + out.toString() + "</pre>");
    } else if (STDERR.equalsIgnoreCase(consoleStream)) {
      stream.addToBody("<hr/>" + label("Console Error") + "<hr/>");
      stream.addToBody("<pre/>" + err.toString() + "</pre>");
    } else {
      throw new FitFailureException("Don't know how to process "
        + consoleStream
        + ", valid values are: <pre>stdout, stderr</pre>");
    }
  }

  public final boolean andEnsureContains(
  final String streamName, final String text) {
    if (STDOUT.equalsIgnoreCase(streamName)) {
      return out.toString().contains(text);
    } else if (STDERR.equalsIgnoreCase(streamName)) {
      return err.toString().contains(text);
    } else {
      throw new FitFailureException("Don't know how to process "
        + streamName
        + ", valid values are: <pre>stdout, stderr</pre>");
    }
  }
}