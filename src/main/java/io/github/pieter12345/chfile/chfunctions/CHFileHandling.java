package io.github.pieter12345.chfile.chfunctions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Security;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CRESecurityException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.natives.interfaces.Mixed;

import io.github.pieter12345.chfile.LifeCycle.FileFunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * CHFile's CHFileHandling functions class.
 * The functions in this file can be used in MethodScript.
 * @author P.J.S. Kools
 */
public class CHFileHandling {
	
	@api(environments = {CommandHelperEnvironment.class})
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
				throw new CREIOException("The location does not exist: " + location.getAbsolutePath() + ".", t);
			}
			if(!location.isDirectory()) {
				throw new CREIOException("The location is not a directory: " + location.getAbsolutePath() + ".", t);
			}
			CArray ret = new CArray(t);
			for(String content : location.list()) {
				ret.push(new CString(content, t), t);
			}
			return ret;
		}
		
		@Override
		public String docs() {
			return "array {directory} Lists all files and directories in the given directory and returns them in an array. The path is relative to"
				+ " the file that is being run, not CommandHelper."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown.";
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
	
	@api(environments = {CommandHelperEnvironment.class})
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
			return "boolean {filename} Returns whether the given file or directory exists. The path is relative to"
				+ " the file that is being run, not CommandHelper."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown.";
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
	
	@api(environments = {CommandHelperEnvironment.class})
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
			return "boolean {filename} Returns whether the given file is a directory. The path is relative to"
				+ " the file that is being run, not CommandHelper."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown.";
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
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class chf_copy extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {2, 3, 4};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File locationFrom = Static.GetFileFromArgument(args[0].val(), env, t, null);
			File locationTo = Static.GetFileFromArgument(args[1].val(), env, t, null);
			boolean overWrite = args.length == 3 && ArgumentValidation.getBooleanish(args[2], t);
			boolean createTargetDirs = args.length == 4 && ArgumentValidation.getBooleanish(args[3], t);
			checkSecurity(locationFrom, env, t);
			checkSecurity(locationTo, env, t);
			
// Handled in copyFile().
//			// Check if there already is a file at locationTo (prevent overwriting when it's not desired).
//			if(!overWrite && locationTo.exists() && locationTo.isFile()) {
//				throw new CRE("Trying to copy to an existing file without overwrite enabled: '" + locationTo.getAbsolutePath() + "'",
//					Exceptions.ExceptionType.SecurityException, t);
//			}
			
			// Check if the file/directory at locationFrom exists.
			if(!locationFrom.exists()) {
				throw new CREIOException("The given file or directory does not exist: '" + locationFrom.getAbsolutePath() + "'", t);
			}
			
			// Check if the file/directory at locationTo exists if the locationFrom is a directory.
			if(locationFrom.isDirectory() && !locationTo.exists()) {
				if(createTargetDirs) {
					if(!locationTo.mkdirs()) {
						throw new CREIOException("Could not create folder to copy to: '" + locationTo.getAbsolutePath() + "'", t);
					}
				} else {
					throw new CREIOException("Could not copy because the target directory does not exist: '" + locationTo.getAbsolutePath() + "'", t);
				}
			}
			
			// Check if the locationFrom is the same as the locationTo (For files only, directories can be put in there).
			if(locationFrom.isFile() && locationFrom.getAbsolutePath().equals(locationTo.getAbsolutePath())) {
				throw new CREIOException("Trying to copy from a file to the same file at: '" + locationFrom.getAbsolutePath() + "'", t);
			}
			
			// Perform the copy.
			try {
				copyFile(locationFrom, locationTo, overWrite, t);
			} catch(IOException  e) {
				throw new CREIOException("Could not copy (some) files from: '" + locationFrom.getAbsolutePath() + "' to: '" + locationTo.getAbsolutePath() + "'", t);
			}
			
			return CVoid.VOID;
		}
		
		// copyFile method.
		// Copies the "from" file to the "to" file, overwriting any existing files. Folders are merged is they already exist.
		private static void copyFile(File from, File to, boolean overWrite, Target t) throws IOException, ConfigRuntimeException {
			if(from.isFile()) {
				
				// Check if there already is a file at locationTo (prevent overwriting when it's not desired).
				if(!overWrite && to.exists()) {
					throw new CRESecurityException("Trying to copy to an existing file without overwrite enabled: '" + to.getAbsolutePath() + "'", t);
				}
				
				Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else if(from.isDirectory()) {
				File toFolder = new File(to.getAbsoluteFile() + "/" + from.getName());
				toFolder.mkdir();
				for(File subFrom : from.listFiles()) {
					File subTo = new File(toFolder.getAbsolutePath() + "/" + subFrom.getName());
					
					// Check if there already is a file at locationTo (prevent overwriting when it's not desired).
					if(!overWrite && subTo.exists()) {
						throw new CRESecurityException("Trying to copy to an existing file without overwrite enabled: '" + subTo.getAbsolutePath() + "'", t);
					}
					if(subFrom.isFile()) {
						copyFile(subFrom, subTo, overWrite, t);
					} else if(subFrom.isDirectory()) {
						copyFile(subFrom, toFolder, overWrite, t);
					}
				}
			}
		}
		
		@Override
		public String docs() {
			return "void {fromFilePath, toFilePath, [allowOverwrite], [createRequiredDirs]} Copies the file (or directory) from the fromFilePath to the toFilePath."
				+ " The paths are relative to the file that is being run, not CommandHelper."
				+ " Throws a SecurityException if allowOverwrite is false and the file at toFilename already exists and is not a directory. Directories are merged."
				+ " Throws an IOException if createRequiredDirs is false and the directory at toFilename does not exist."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown.";
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
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class chf_delete extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {1, 2};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			boolean allowRemoveFolderContent = args.length == 2 && ArgumentValidation.getBooleanish(args[1], t);
			checkSecurity(location, env, t);
			
			// Check if the file/directory at the location exists.
			if(!location.exists()) {
				throw new CREIOException("The given file does not exist: '" + location.getAbsolutePath() + "'", t);
			}
			
			// Check if the file is a non-empty directory and allowRemoveFolderContent is false.
			if(!allowRemoveFolderContent && location.isDirectory() && location.listFiles().length != 0) {
				throw new CRESecurityException("The given file is a non-empty directory and allowRemoveFolderContent is not enabled: '" + location.getAbsolutePath() + "'", t);
			}
			
			// Perform the deletion.
			if(!deleteFile(location)) {
				throw new CREIOException("Could not delete (some) file(s) from: '" + location.getAbsolutePath() + "'", t);
			}
			
			return CVoid.VOID;
		}
		
		// deleteFile method.
		// Deletes the given file or folder from the disk.
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
			return "void {filename, [allowRemoveFolderContent]} Deletes the file (or directory) at the given path."
				+ " The path is relative to the file that is being run, not CommandHelper."
				+ " Throws a SecurityException If allowRemoveFolderContent is false and the given file is a non-empty directory."
				+ " Throws an IOException if the file does not exist or (a part of the files) could not be removed."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown.";
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
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class chf_create_file extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {1, 2};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			boolean createRequiredDirs = args.length == 2 && ArgumentValidation.getBooleanish(args[1], t);
			checkSecurity(location, env, t);
			
			// Check if the file/directory at the location exists.
			if(location.exists()) {
				throw new CREIOException("The given file already exists: '" + location.getAbsolutePath() + "'", t);
			}
			
			// Check if the parent directory has to be created and createRequiredDirs has not been enabled.
			if(!location.getParentFile().exists()) {
				if(!createRequiredDirs) {
					throw new CRESecurityException("The directory in which the file would be creates does not exist and createRequiredDirs is not enabled: '" + location.getAbsolutePath() + "'", t);
				} else {
					if(!location.getParentFile().mkdirs()) { // Create folders.
						throw new CREIOException("Could not create (some) directory(ies) of: '" + location.getAbsolutePath() + "'", t);
					}
				}
			}
			
			// Perform the file creation.
			try {
				location.createNewFile();
			} catch(IOException e) {
				throw new CREIOException("Could not create file at: '" + location.getAbsolutePath() + "'", t);
			}
			
			return CVoid.VOID;
		}
		
		@Override
		public String docs() {
			return "void {filename, [createRequiredDirs]} Creates a file at the given path."
				+ " Will create required directory or throw a SecurityException based on the createRequiredDirs boolean."
				+ " The path is relative to the file that is being run, not CommandHelper."
				+ " Throws a SecurityException If createRequiredDirs is false and the given file is in an unexisting directory."
				+ " Throws an IOException if the file or required directories could not be created."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown.";
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
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class chf_create_directory extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {1, 2};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File location = Static.GetFileFromArgument(args[0].val(), env, t, null);
			boolean createRequiredDirs = args.length == 2 && ArgumentValidation.getBooleanish(args[1], t);
			checkSecurity(location, env, t);
			
			// Return if a directory at the location already exists. Throw an exception if it's a file.
			if(location.exists()) {
				if(location.isDirectory()) {
					return CVoid.VOID;
				} else {
					throw new CREIOException("Can not create a folder with the same name as a file in the same directory: '" + location + "'", t);
				}
			}
			
			// Check if the parent directory has to be created and createRequiredDirs has not been enabled.
			File parentFile = location.getParentFile();
			if(parentFile != null) {
				if(!parentFile.exists()) {
					if(!createRequiredDirs) {
						throw new CRESecurityException("The directory in which the directory would be creates does not exist and createRequiredDirs is not enabled: '" + location.getAbsolutePath() + "'", t);
					} else {
						if(!parentFile.mkdirs()) { // Create folders.
							throw new CREIOException("Could not create (some) directory(ies) of: '" + location.getAbsolutePath() + "'", t);
						}
					}
				}
			}
			
			// Perform the directory creation.
			if(!location.mkdir()) {
				throw new CREIOException("Could not create folder at: '" + location.getAbsolutePath() + "'", t);
			}
			
			return CVoid.VOID;
		}
		
		@Override
		public String docs() {
			return "void {foldername, [createRequiredDirs]} Creates a directory at the given path."
				+ " Will create required directories or throw a SecurityException based on the createRequiredDirs boolean."
				+ " The path is relative to the file that is being run, not CommandHelper."
				+ " Throws a SecurityException If createRequiredDirs is false and the given folder is in an unexisting directory."
				+ " Throws an IOException if the directory or required directories could not be created."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown.";
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
	
	@api(environments = {CommandHelperEnvironment.class})
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
					throw new CRESecurityException("The file already exists and no OVERWRITE option has been found: " + location.getAbsolutePath() + ".", t);
				}
				options = new OpenOption[] {StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW};
			} else if(writeOption.equalsIgnoreCase("APPEND")) {
				options = new OpenOption[] {StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND};
			} else if(writeOption.equalsIgnoreCase("OVERWRITE")) {
				options = new OpenOption[] {StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
			} else {
				throw new CREFormatException("Argument 3 has to be one of OVERWRITE or APPEND.", t);
			}
			location.getParentFile().mkdirs();
			byte[] bytes = content.replaceAll("\r\n", "\n").replaceAll("\n", "\r\n").getBytes(); // Force "\r\n" newlines.
			try {
				Files.write(location.toPath(), bytes, options);
			} catch (IOException ex) {
				throw new CREIOException("Could not write to file.", t);
			}
			
			return CVoid.VOID;
		}
		
		@Override
		public String docs() {
			return "void {filename, content, [option]} Writes the given content to the given file."
				+ " The option can be one of OVERWRITE/APPEND. If whe file exists already and no option is given, a SecurityException is thrown."
				+ " The path is relative to the file that is being run, not CommandHelper. Newlines are forced to \\r\\n"
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
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
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class chf_rename extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {2}; // TODO Add 3 once the move with overwrite works.
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File locationOld = Static.GetFileFromArgument(args[0].val(), env, t, null);
			File locationNew = Static.GetFileFromArgument(args[1].val(), env, t, null);
			boolean allowOverwrite = args.length == 3 && ArgumentValidation.getBooleanish(args[2], t);
			checkSecurity(locationOld, env, t);
			checkSecurity(locationNew, env, t);
			
			// Check if the parent directory is the same (it should be as this is a rename only).
			if(!locationOld.getParentFile().getAbsolutePath().equals(locationNew.getParentFile().getAbsolutePath())) {
				throw new CRESecurityException("The new filename has to be in the same directory.", t);
			}
			
			// Check if the old location exists.
			if(!locationOld.exists()) {
				throw new CREIOException("The file to rename does not exist: " + locationOld.getAbsolutePath() + ".", t);
			}
			
			// Check if the new location already exists without overwrite enabled.
			if(locationNew.exists() && !allowOverwrite) {
				throw new CRESecurityException("The file already exists and no OVERWRITE option has been found: " + locationNew.getAbsolutePath() + ".", t);
			}
			
			// Rename the file.
			if(!locationOld.renameTo(locationNew)) {
				throw new CREIOException("Could not rename file: " + locationOld.getAbsolutePath() + " to " + locationNew.getAbsolutePath() + ".", t);
			}
			
			return CVoid.VOID;
		}
		
		@Override
		public String docs() {
			return "void {filename, newfilename, [overwrite]} Renames the filename to newfilename."
				+ " If whe file already exists and overwrite is false, a SecurityException is thrown."
				+ " The path is relative to the file that is being run, not CommandHelper."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
			    + " If the renaming itself fails, an IOException is thrown.";
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
	
	@api(environments = {CommandHelperEnvironment.class})
	public static class chf_move extends FileFunction {
		
		@Override
		public Integer[] numArgs() {
			return new Integer[] {2, 3};
		}
		
		@Override
		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			File locationOld = Static.GetFileFromArgument(args[0].val(), env, t, null);
			File locationNew = Static.GetFileFromArgument(args[1].val(), env, t, null);
			boolean allowOverwrite = args.length == 3 && ArgumentValidation.getBooleanish(args[2], t);
			checkSecurity(locationOld, env, t);
			checkSecurity(locationNew, env, t);
			
			// Check if the locations are the same (Just return if the move doesn't do anything).
			if(locationOld.getAbsolutePath().equals(locationNew.getAbsolutePath())) {
				return CVoid.VOID;
			}
			
			// Check if the old location exists.
			if(!locationOld.exists()) {
				throw new CREIOException("The file to move does not exist: " + locationOld.getAbsolutePath() + ".", t);
			}
			
			// Check if the new location already exists without overwrite enabled.
			if(locationNew.exists() && !allowOverwrite) {
				throw new CRESecurityException("The file already exists and no OVERWRITE option has been found: " + locationNew.getAbsolutePath() + ".", t);
			}
			
			// Move the file.
			try {
				if(allowOverwrite) {
					moveFile(locationOld, locationNew, StandardCopyOption.REPLACE_EXISTING);
				} else {
					moveFile(locationOld, locationNew);
				}
			} catch (IOException e) {
				throw new CREIOException("Could not move one or more file(s) from: " + locationOld.getAbsolutePath()
						+ " to " + locationNew.getAbsolutePath() + ". Reason: " + e.getMessage(), t);
			}
			
			return CVoid.VOID;
		}
		
		private static void moveFile(File locationOld, File locationNew, CopyOption... options) throws IOException {
			if(locationOld.isFile()) {
				Files.move(locationOld.toPath(), locationNew.toPath(), options);
			} else {
				for(File subOld : locationOld.listFiles()) {
					File subNew = new File(locationNew.getAbsolutePath() + "/" + subOld.getName());
					if(!subNew.mkdir()) {
						throw new IOException("Could not create directory (does its parent exist?): " + subNew.getAbsolutePath());
					}
					moveFile(subOld, subNew, options);
				}
			}
		}
		
		@Override
		public String docs() {
			return "void {filename, newfilename, [overwrite]} Renames the filename to newfilename."
				+ " If whe file already exists and overwrite is false, a SecurityException is thrown."
				+ " The path is relative to the file that is being run, not CommandHelper."
				+ " If the file specified is not within base-dir (as specified in the preferences file), a SecurityException is thrown."
			    + " If the renaming itself fails, an IOException is thrown.";
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
				throw new CRESecurityException("You do not have permission to access file: '" + file.getAbsolutePath() + "'", t);
			}
		} catch (IOException e) {
			throw new CREIOException(e.getMessage(), t);
		}
	}
}
