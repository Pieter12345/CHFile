package io.github.pieter12345.chfile;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import com.laytonsmith.core.functions.AbstractFunction;

/**
 * CHFile's LifeCycle.
 * @author Pieter12345
 * @since 24-04-2015
 */
@MSExtension("CHFile")
public class LifeCycle extends AbstractExtension {
	
	@Override
	public void onStartup() {
		System.out.println("CHFile " + this.getVersion() + " loaded.");
	}
	
	@Override
	public void onShutdown() {
		System.out.println("CHFile " + this.getVersion() + " unloaded.");
	}
	
	@Override
	public Version getVersion() {
		return new SimpleVersion(0, 0, 5);
	}
	
	public static abstract class FileFunction extends AbstractFunction {
		
		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}
		
		@Override
		public boolean isRestricted() {
			return true;
		}
		
		@Override
		public Boolean runAsync() {
			return true; // Disk IO may run asynchronously.
		}
		
		@Override
		public LogLevel profileAt() {
			return LogLevel.DEBUG;
		}
	}
}