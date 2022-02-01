package io.github.pieter12345.chfile.chfunctions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.natives.interfaces.Mixed;

import io.github.pieter12345.chfile.LifeCycle.FileFunction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPOutputStream;

/**
 * CHFile's CHFileHandling functions class.
 * The functions in this file can be used in MethodScript.
 * @author P.J.S. Kools
 */
public class CHFileHandling {
	
	@api
	public static class chf_directory_list extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {1};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			checkSecurity(location, env, t);
			if(!location.exists()) {
				throw new CREIOException(
						"Directory at location does not exist: " + location.getAbsolutePath() + ".", t);
			}
			if(!location.isDirectory()) {
				throw new CREIOException("File at location is not a directory: " + location.getAbsolutePath() + ".", t);
			}
			CArray ret = new CArray(t);
			for(String content : location.list()) {
				ret.push(new CString(content, t), t);
			}
			return ret;
		}
		
		@Override
		public String docs() {
			return "array {directory} Returns an array containing all files and directories in the given directory."
					+ " The path is relative to the file that is being run, not CommandHelper."
					+ " If the file specified is not within base-dir (as specified in the preferences file),"
					+ " a SecurityException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRESecurityException.class, CREIOException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	@api
	public static class chf_file_exists extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {1};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			checkSecurity(location, env, t);
			return CBoolean.GenerateCBoolean(location.exists(), t);
		}
		
		@Override
		public String docs() {
			return "boolean {path} Returns whether the file or directory at the given path exists."
					+ " The path is relative to the file that is being run, not CommandHelper."
					+ " If the file specified is not within base-dir (as specified in the preferences file),"
					+ " a SecurityException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRESecurityException.class, CREIOException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	@api
	public static class chf_is_directory extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {1};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			checkSecurity(location, env, t);
			return CBoolean.GenerateCBoolean(location.isDirectory(), t);
		}
		
		@Override
		public String docs() {
			return "boolean {path} Returns whether the file at the given path is a directory."
					+ " The path is relative to the file that is being run, not CommandHelper."
					+ " If the file specified is not within base-dir (as specified in the preferences file),"
					+ " a SecurityException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRESecurityException.class, CREIOException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	@api
	public static class chf_copy extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {2, 3, 4};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File locationFrom = Static.GetFileFromArgument(args[0].val(), env, t, null);
			File locationTo = Static.GetFileFromArgument(args[1].val(), env, t, null);
			boolean overWrite = args.length >= 3 && ArgumentValidation.getBooleanObject(args[2], t);
			boolean createTargetDirs = args.length == 4 && ArgumentValidation.getBooleanObject(args[3], t);
			checkSecurity(locationFrom, env, t);
			checkSecurity(locationTo, env, t);
			
			// Disallow copying a file/directory to itself.
			if(locationFrom.getAbsolutePath().equals(locationTo.getAbsolutePath())) {
				throw new CREIOException(
						"Cannot copy file or directory to itself: '" + locationFrom.getAbsolutePath() + "'", t);
			}
			
			// Check if the file/directory at locationFrom exists.
			if(!locationFrom.exists()) {
				throw new CREIOException(
						"File or directory at 'fromPath' does not exist: '" + locationFrom.getAbsolutePath() + "'", t);
			}
			
			// Check if the file/directory at locationTo exists if the locationFrom is a directory.
			File locationToParent = locationTo.getParentFile();
			if(locationToParent != null && !locationToParent.exists()) {
				if(!createTargetDirs) {
					throw new CREIOException(
							"Target directory does not exist: '" + locationToParent.getAbsolutePath() + "'", t);
				}
				if(!locationToParent.mkdirs()) {
					throw new CREIOException(
							"Could not create directory: '" + locationToParent.getAbsolutePath() + "'", t);
				}
			}
			
			// Perform the copy.
			if(locationFrom.isFile()) {
				try {
					copyFile(locationFrom, locationTo, overWrite, t);
				} catch (IOException e) {
					throw new CREIOException("Could not copy file from: '" + locationFrom.getAbsolutePath()
							+ "' to: '" + locationTo.getAbsolutePath() + "'. Message: " + e.getMessage(), t);
				}
			} else {
				try {
					if(!locationTo.exists() && !locationTo.mkdir()) {
						throw new CREIOException(
								"Could not create directory: '" + locationToParent.getAbsolutePath() + "'", t);
					}
					for(File fromFile : locationFrom.listFiles()) {
						File toFile = new File(locationTo, fromFile.getName());
						copyFile(fromFile, toFile, overWrite, t);
					}
				} catch (IOException e) {
					throw new CREIOException("Could not copy (some) file(s) from: '" + locationFrom.getAbsolutePath()
							+ "' to: '" + locationTo.getAbsolutePath() + "'. Message: " + e.getMessage(), t);
				}
			}
			
			return CVoid.VOID;
		}
		
		/**
		 * Copies the 'from' file or directory to the 'to' path, overwriting any existing files.
		 * The name for the file or directory copy should be provided in the 'to' path.
		 * Directories are merged if they already exist.
		 * @param from - The from file or directory.
		 * @param to - The to file or directory.
		 * @param overWrite - If {@code true}, already existing files will be overwritten.
		 * @param t - The target.
		 * @throws IOException When an I/O error occurs when copying a file or directory.
		 * @throws CRESecurityException When the security manager disallows (a part of) the copy action.
		 */
		private static void copyFile(File from, File to, boolean overWrite, Target t)
				throws IOException, CRESecurityException {
			if(from.isFile()) {
				
				// Prevent file overwriting if it's not allowed.
				if(!overWrite && to.isFile()) {
					throw new CRESecurityException("Cannot overwrite existing file (overwrite parameter is false): '"
							+ to.getAbsolutePath() + "'", t);
				}
				
				Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else if(from.isDirectory()) {
				
				// Create the 'to' directory.
				if(!to.isDirectory() && !to.mkdir()) {
					throw new IOException("Could not create directory: '" + to.getAbsolutePath() + "'.");
				}
				
				// Copy the directory contents.
				for(File subFrom : from.listFiles()) {
					File subTo = new File(to.getAbsoluteFile(), subFrom.getName());
					
					// Prevent file overwriting if it's not allowed.
					if(!overWrite && subTo.isFile()) {
						throw new CRESecurityException("Cannot overwrite existing file"
								+ " (overwrite parameter is false): '" + subTo.getAbsolutePath() + "'", t);
					}
					copyFile(subFrom, subTo, overWrite, t);
				}
			}
		}
		
		@Override
		public String docs() {
			return "void {fromPath, toPath, [allowOverwrite], [createRequiredDirs]}"
					+ " Copies the file or directory (including contents) from the fromPath to the toPath."
					+ " When copying a directory which's target already exists,"
					+ " it will be merged with the existing directory. This also holds for subdirectories."
					+ " toPath should contain the file or directory name of the copy,"
					+ " and not just the directory in which to place the copy."
					+ " If allowOverwrite is true, files will overwrite the file at their target location if they"
					+ " already exist. Defaults to false."
					+ " If createRequiredDirs is true, the parent directory of toPath will be created if it does not"
					+ " yet exist. Defaults to false."
					+ " The paths are relative to the file that is being run, not CommandHelper."
					+ " Throws a SecurityException if allowOverwrite is false and the file at toPath already exists"
					+ " and is not a directory."
					+ " Throws an IOException if createRequiredDirs is false and the parent directory of toPath does"
					+ " not exist."
					+ " If the file specified is not within base-dir (as specified in the preferences file),"
					+ " a SecurityException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRESecurityException.class, CREIOException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	@api
	public static class chf_delete extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {1, 2};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			boolean allowRemoveDirContent = args.length == 2 && ArgumentValidation.getBooleanObject(args[1], t);
			checkSecurity(location, env, t);
			
			// Check if the file/directory at the location exists.
			if(!location.exists()) {
				throw new CREIOException("The given file does not exist: '" + location.getAbsolutePath() + "'", t);
			}
			
			// Check if the file is a non-empty directory and allowRemoveFolderContent is false.
			if(!allowRemoveDirContent && location.isDirectory() && location.listFiles().length != 0) {
				throw new CRESecurityException("The given file is a non-empty directory and allowRemoveFolderContent"
						+ " is not enabled: '" + location.getAbsolutePath() + "'", t);
			}
			
			// Perform the deletion.
			if(!deleteFile(location)) {
				throw new CREIOException(
						"Could not delete (some) file(s) from: '" + location.getAbsolutePath() + "'", t);
			}
			
			return CVoid.VOID;
		}
		
		/**
		 * Deletes the given file or directory.
		 * @param file - The file to delete.
		 * @return {@code true} on success, {@code false} when at least one file could not be deleted.
		 */
		private static boolean deleteFile(File file) {
			if(file.isFile()) {
				return file.delete();
			} else if(file.isDirectory()) {
				boolean success = true;
				for(File subFile : file.listFiles()) {
					if(!deleteFile(subFile)) {
						success = false; // Don't just throw an exception right away to delete as much as possible.
					}
				}
				return success && file.delete(); // Also remove the containing folder.
			}
			return false; // Should be impossible to reach.
		}
		
		@Override
		public String docs() {
			return "void {path, [allowRemoveDirContent]} Deletes the file or directory at the given path."
				+ " The path is relative to the file that is being run, not CommandHelper."
				+ " If allowRemoveDirContent is true, directory contents will be removed if a non-empty directory is"
				+ " given. Defaults to false."
				+ " Throws a SecurityException If allowRemoveDirContent is false and the given file is a"
				+ " non-empty directory."
				+ " Throws an IOException if the file does not exist or (a part of the files) could not be removed."
				+ " If the file specified is not within base-dir (as specified in the preferences file),"
				+ " a SecurityException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRESecurityException.class, CREIOException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	@api
	public static class chf_create_file extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {1, 2};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			boolean createRequiredDirs = args.length == 2 && ArgumentValidation.getBooleanObject(args[1], t);
			checkSecurity(location, env, t);
			
			// Check if the file/directory at the location exists.
			if(location.exists()) {
				throw new CREIOException("The given file already exists: '" + location.getAbsolutePath() + "'", t);
			}
			
			// Check if the parent directory has to be created and createRequiredDirs has not been enabled.
			if(!location.getParentFile().exists()) {
				if(!createRequiredDirs) {
					throw new CRESecurityException("The directory in which the file would be created does not exist"
							+ " and createRequiredDirs is not enabled: '" + location.getAbsolutePath() + "'", t);
				} else {
					if(!location.getParentFile().mkdirs()) {
						throw new CREIOException(
								"Could not create directory: '" + location.getParentFile().getAbsolutePath() + "'", t);
					}
				}
			}
			
			// Perform the file creation.
			try {
				location.createNewFile();
			} catch (IOException e) {
				throw new CREIOException("Could not create file at: '"
						+ location.getAbsolutePath() + "'. Message: "  + e.getMessage(), t);
			}
			
			return CVoid.VOID;
		}
		
		@Override
		public String docs() {
			return "void {path, [createRequiredDirs]} Creates a file at the given path."
				+ " If createRequiredDirs is true, required parent directories will be created. Defaults to false."
				+ " The path is relative to the file that is being run, not CommandHelper."
				+ " Throws a SecurityException if createRequiredDirs is false and the parent directory of the given"
				+ " path does not exist."
				+ " Throws an IOException if the file or required directories could not be created."
				+ " If the file specified is not within base-dir (as specified in the preferences file),"
				+ " a SecurityException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRESecurityException.class, CREIOException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	@api
	public static class chf_create_directory extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {1, 2};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			boolean createRequiredDirs = args.length == 2 && ArgumentValidation.getBooleanObject(args[1], t);
			checkSecurity(location, env, t);
			
			// Return if a directory at the location already exists. Throw an exception if it's a file.
			if(location.exists()) {
				if(location.isDirectory()) {
					return CVoid.VOID;
				} else {
					throw new CREIOException("Cannot create directory with the same name as a file"
							+ " in the same directory: '" + location + "'", t);
				}
			}
			
			// Check if the parent directory has to be created and createRequiredDirs has not been enabled.
			File parentFile = location.getParentFile();
			if(parentFile != null) {
				if(!parentFile.exists()) {
					if(!createRequiredDirs) {
						throw new CRESecurityException("The directory in which the directory would be created does"
								+ " not exist and createRequiredDirs is not enabled: '"
								+ location.getAbsolutePath() + "'", t);
					} else {
						if(!parentFile.mkdirs()) {
							throw new CREIOException("Could not create (some) directory(ies) of: '"
									+ location.getAbsolutePath() + "'", t);
						}
					}
				}
			}
			
			// Perform the directory creation.
			if(!location.mkdir()) {
				throw new CREIOException("Could not create directory at: '" + location.getAbsolutePath() + "'", t);
			}
			
			return CVoid.VOID;
		}
		
		@Override
		public String docs() {
			return "void {path, [createRequiredDirs]} Creates a directory at the given path."
					+ " If createRequiredDirs is true, required parent directories will be created. Defaults to false."
					+ " The path is relative to the file that is being run, not CommandHelper."
					+ " Throws a SecurityException if createRequiredDirs is false and the parent directory of the given"
					+ " path does not exist."
					+ " Throws an IOException if the file or required directories could not be created."
					+ " If the file specified is not within base-dir (as specified in the preferences file),"
					+ " a SecurityException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRESecurityException.class, CREIOException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	@api
	public static class chf_write extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {2, 3};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			String content = args[1].val();
			String writeOption = (args.length < 3 ? null : args[2].val());
			checkSecurity(location, env, t);
			OpenOption[] options;
			if(writeOption == null) {
				if(location.exists()) {
					throw new CRESecurityException("The file already exists and no OVERWRITE option has been given: '"
							+ location.getAbsolutePath() + "'.", t);
				}
				options = new OpenOption[] {StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW};
			} else if(writeOption.equalsIgnoreCase("APPEND")) {
				options = new OpenOption[] {
						StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND};
			} else if(writeOption.equalsIgnoreCase("OVERWRITE")) {
				options = new OpenOption[] {
						StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
			} else {
				throw new CREFormatException(
						"Argument 3 of " + this.getName() + " has to be one of 'OVERWRITE' or 'APPEND'.", t);
			}
			location.getParentFile().mkdirs();
			try {
				Files.write(location.toPath(), content.getBytes(), options);
			} catch (IOException e) {
				throw new CREIOException("Could not write to file. Message: " + e.getMessage(), t);
			}
			
			return CVoid.VOID;
		}
		
		@Override
		public String docs() {
			return "void {path, content, [option]} Writes the given content to the file at the given path."
				+ " The option can be one of OVERWRITE/APPEND."
				+ " Required parent directories will be created if necessary."
				+ " If the file already exists and no option is given, a SecurityException is thrown."
				+ " The path is relative to the file that is being run, not CommandHelper."
				+ " If the file specified is not within base-dir (as specified in the preferences file),"
				+ " a SecurityException is thrown."
			    + " If the writing itself fails, an IOException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {CRESecurityException.class, CREIOException.class, CREFormatException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	@api
	public static class chf_write_gzip_binary extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {2, 3};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			CByteArray content = ArgumentValidation.getByteArray(args[1], t);
			boolean overwrite = args.length >= 3 && ArgumentValidation.getBooleanish(args[2], t);
			checkSecurity(location, env, t);
			if(!overwrite && location.exists()) {
				throw new CRESecurityException("The file already exists and the overwrite option is false: '"
						+ location.getAbsolutePath() + "'.", t);
			}
			location.getParentFile().mkdirs();
			try(OutputStream outStream = new GZIPOutputStream(new FileOutputStream(location))) {
				outStream.write(content.asByteArrayCopy());
			} catch (IOException e) {
				throw new CREIOException("Could not write to file. Message: " + e.getMessage(), t);
			}
			
			return CVoid.VOID;
		}
		
		@Override
		public String docs() {
			return "void {path, content, [overwrite]}"
					+ " Gzips and writes the given byte array to the file at the given path."
					+ " Required parent directories will be created if necessary."
					+ " If the file already exists and overwrite is false, a SecurityException is thrown."
					+ " Overwrite defaults to false."
					+ " The path is relative to the file that is being run, not CommandHelper."
					+ " If the content is not a byte_array, a CastException is thrown."
					+ " If the file specified is not within base-dir (as specified in the preferences file),"
					+ " a SecurityException is thrown."
				    + " If the writing itself fails, an IOException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {
					CRECastException.class, CRESecurityException.class, CREIOException.class, CREFormatException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	@api
	public static class chf_write_binary extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {2, 3};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			CByteArray content = ArgumentValidation.getByteArray(args[1], t);
			boolean overwrite = args.length >= 3 && ArgumentValidation.getBooleanish(args[2], t);
			checkSecurity(location, env, t);
			if(!overwrite && location.exists()) {
				throw new CRESecurityException("The file already exists and the overwrite option is false: '"
						+ location.getAbsolutePath() + "'.", t);
			}
			location.getParentFile().mkdirs();
			try(OutputStream outStream = new BufferedOutputStream(new FileOutputStream(location))) {
				outStream.write(content.asByteArrayCopy());
			} catch (IOException e) {
				throw new CREIOException("Could not write to file. Message: " + e.getMessage(), t);
			}
			
			return CVoid.VOID;
		}
		
		@Override
		public String docs() {
			return "void {path, content, [overwrite]}"
					+ " Writes the given byte array to the file at the given path."
					+ " Required parent directories will be created if necessary."
					+ " If the file already exists and overwrite is false, a SecurityException is thrown."
					+ " Overwrite defaults to false."
					+ " The path is relative to the file that is being run, not CommandHelper."
					+ " If the content is not a byte_array, a CastException is thrown."
					+ " If the file specified is not within base-dir (as specified in the preferences file),"
					+ " a SecurityException is thrown."
				    + " If the writing itself fails, an IOException is thrown.";
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[] {
					CRECastException.class, CRESecurityException.class, CREIOException.class, CREFormatException.class};
		}
		
		@Override
		public Version since() {
			return MSVersion.V3_3_1;
		}
	}
	
	// TODO - Decide what to do with this. If re-adding this, then it should be platform independent.
//	@api
//	public static class chf_rename extends FileFunction {
//		
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[] {2}; // TODO Add 3 once the move with overwrite works.
//		}
//		
//		@Override
//		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
//			File locationOld = Static.GetFileFromArgument(args[0].val(), env, t, null);
//			File locationNew = Static.GetFileFromArgument(args[1].val(), env, t, null);
//			boolean allowOverwrite = args.length == 3 && ArgumentValidation.getBooleanObject(args[2], t);
//			checkSecurity(locationOld, env, t);
//			checkSecurity(locationNew, env, t);
//			
//			// Check if the parent directory is the same (it should be as this is a rename only).
//			if(!locationOld.getParentFile().getAbsolutePath().equals(locationNew.getParentFile().getAbsolutePath())) {
//				throw new CRESecurityException("The new filename has to be in the same directory.", t);
//			}
//			
//			// Check if the old location exists.
//			if(!locationOld.exists()) {
//				throw new CREIOException(
//						"The file to rename does not exist: " + locationOld.getAbsolutePath() + ".", t);
//			}
//			
//			// Check if the new location already exists without overwrite enabled.
//			if(locationNew.exists() && !allowOverwrite) {
//				throw new CRESecurityException("The file already exists and no OVERWRITE option has been found: "
//						+ locationNew.getAbsolutePath() + ".", t);
//			}
//			
//			// Rename the file.
//			if(!locationOld.renameTo(locationNew)) {
//				throw new CREIOException("Could not rename file: " + locationOld.getAbsolutePath()
//						+ " to " + locationNew.getAbsolutePath() + ".", t);
//			}
//			
//			return CVoid.VOID;
//		}
//		
//		@Override
//		public String docs() {
//			return "void {filename, newfilename, [overwrite]} Renames the filename to newfilename."
//				+ " If the file already exists and overwrite is false, a SecurityException is thrown."
//				+ " The path is relative to the file that is being run, not CommandHelper."
//				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
//			    + " If the renaming itself fails, an IOException is thrown.";
//		}
//		
//		@Override
//		@SuppressWarnings("unchecked")
//		public Class<? extends CREThrowable>[] thrown() {
//			return new Class[] {CRESecurityException.class, CREIOException.class};
//		}
//		
//		@Override
//		public Version since() {
//			return MSVersion.V3_3_1;
//		}
//	}
	
	// TODO - Decide what to do with this. Moving directories with contents only sometimes works without copying.
//	@api
//	public static class chf_move extends FileFunction {
//		
//		@Override
//		public Integer[] numArgs() {
//			return new Integer[] {2, 3};
//		}
//		
//		@Override
//		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
//			File locationOld = Static.GetFileFromArgument(args[0].val(), env, t, null);
//			File locationNew = Static.GetFileFromArgument(args[1].val(), env, t, null);
//			boolean allowOverwrite = args.length == 3 && ArgumentValidation.getBooleanObject(args[2], t);
//			checkSecurity(locationOld, env, t);
//			checkSecurity(locationNew, env, t);
//			
//			// Check if the locations are the same (Just return if the move doesn't do anything).
//			if(locationOld.getAbsolutePath().equals(locationNew.getAbsolutePath())) {
//				return CVoid.VOID;
//			}
//			
//			// Check if the old location exists.
//			if(!locationOld.exists()) {
//				throw new CREIOException("The file to move does not exist: " + locationOld.getAbsolutePath() + ".", t);
//			}
//			
//			// Check if the new location already exists without overwrite enabled.
//			if(locationNew.exists() && !allowOverwrite) {
//				throw new CRESecurityException("Cannot overwrite existing file (overwrite is false): "
//						+ locationNew.getAbsolutePath() + ".", t);
//			}
//			
//			// Move the file.
//			// TODO - Test this. Directories are moved when the OS can move them without having to change the contents.
//			try {
//				if(allowOverwrite) {
//					Files.move(locationOld.toPath(), locationNew.toPath(), StandardCopyOption.REPLACE_EXISTING);
////					moveFile(locationOld, locationNew, StandardCopyOption.REPLACE_EXISTING);
//				} else {
//					Files.move(locationOld.toPath(), locationNew.toPath());
////					moveFile(locationOld, locationNew);
//				}
//			} catch (IOException e) {
//				throw new CREIOException("Could not move one or more file(s) from: " + locationOld.getAbsolutePath()
//						+ " to " + locationNew.getAbsolutePath() + ". Reason: " + e.getMessage(), t);
//			}
//			
//			return CVoid.VOID;
//		}
//		
////		private static void moveFile(File locationOld, File locationNew, CopyOption... options) throws IOException {
////			if(locationOld.isFile()) {
////				Files.move(locationOld.toPath(), locationNew.toPath(), options);
////			} else {
////				
////				// Move directory contents.
////				for(File subOld : locationOld.listFiles()) {
////					File subNew = new File(locationNew.getAbsoluteFile(), subOld.getName());
////					if(!subNew.mkdir()) { // TODO - Check if this is the right dir to make. I think this should happen before the loop.
////						throw new IOException("Could not create directory (does its parent exist?): " + subNew.getAbsolutePath());
////					}
////					moveFile(subOld, subNew, options);
////				}
////			}
////		}
//		
//		@Override
//		public String docs() { // TODO - Wrong docs.
//			return "void {filename, newfilename, [overwrite]} Renames the filename to newfilename."
//				+ " If the file already exists and overwrite is false, a SecurityException is thrown."
//				+ " The path is relative to the file that is being run, not CommandHelper."
//				+ " If the file specified is not within base-dir (as specified in the preferences file),"
//				+ " a SecurityException is thrown."
//			    + " If the renaming itself fails, an IOException is thrown.";
//		}
//		
//		@Override
//		@SuppressWarnings("unchecked")
//		public Class<? extends CREThrowable>[] thrown() {
//			return new Class[] {CRESecurityException.class, CREIOException.class};
//		}
//		
//		@Override
//		public Version since() {
//			return MSVersion.V3_3_1;
//		}
//	}
	
	/**
	 * Checks whether the given file may be accessed according to the security manager. In cmdline mode, this is always
	 * allowed.
	 * @param file - The file to check.
	 * @param env - The environment.
	 * @param t - The target.
	 * @throws CRESecurityException - If the security manager disallows usage of the file, given the environment.
	 * @throws CREIOException - If an I/O error occurs while resolving the canonical file path.
	 */
	public static void checkSecurity(File file, Environment env, Target t) throws CRESecurityException, CREIOException {
		try {
			if(!Static.InCmdLine(env, false) && !Security.CheckSecurity(file)) {
				throw new CRESecurityException(
						"You do not have permission to access file: '" + file.getAbsolutePath() + "'", t);
			}
		} catch (IOException e) {
			throw new CREIOException(e.getMessage(), t);
		}
	}
}
