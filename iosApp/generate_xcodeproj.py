#!/usr/bin/env python3
"""
Generates a minimal Xcode project for the iosApp SwiftUI app.
The project includes the shared KMM framework via a Run Script build phase.
"""

import os
import uuid
import json

PROJ_DIR = os.path.dirname(os.path.abspath(__file__))
XCODEPROJ_DIR = os.path.join(PROJ_DIR, "iosApp.xcodeproj")
PBXPROJ_PATH = os.path.join(XCODEPROJ_DIR, "project.pbxproj")

# Use fixed but unique UUIDs for deterministic output
IDs = {
    "project": "A00000000000000000000001",
    "mainGroup": "A00000000000000000000002",
    "productsGroup": "A00000000000000000000003",
    "frameworksGroup": "A00000000000000000000004",
    "target": "A00000000000000000000010",
    "buildConfigList": "A00000000000000000000011",
    "debugConfig": "A00000000000000000000012",
    "releaseConfig": "A00000000000000000000013",
    "targetBuildConfigList": "A00000000000000000000014",
    "targetDebugConfig": "A00000000000000000000015",
    "targetReleaseConfig": "A00000000000000000000016",
    "sourcesBuildPhase": "A00000000000000000000020",
    "resourcesBuildPhase": "A00000000000000000000021",
    "frameworksBuildPhase": "A00000000000000000000022",
    "scriptBuildPhase": "A00000000000000000000023",
    "product": "A00000000000000000000030",
    # Source file refs
    "iOSApp": "A00000000000000000000040",
    "ContentView": "A00000000000000000000041",
    "HomeView": "A00000000000000000000042",
    "TvShowsView": "A00000000000000000000043",
    "SavedView": "A00000000000000000000044",
    "DetailView": "A00000000000000000000045",
    "Components": "A00000000000000000000046",
    "AiSearchView": "A00000000000000000000047",
    # Build file refs
    "iOSApp_bf": "A00000000000000000000050",
    "ContentView_bf": "A00000000000000000000051",
    "HomeView_bf": "A00000000000000000000052",
    "TvShowsView_bf": "A00000000000000000000053",
    "SavedView_bf": "A00000000000000000000054",
    "DetailView_bf": "A00000000000000000000055",
    "Components_bf": "A00000000000000000000056",
    "AiSearchView_bf": "A00000000000000000000057",
    # Info.plist group
    "infoPlist": "A00000000000000000000060",
    "sourcesGroup": "A00000000000000000000070",
}

SWIFT_FILES = [
    ("iOSApp", "iOSApp.swift"),
    ("ContentView", "ContentView.swift"),
    ("HomeView", "HomeView.swift"),
    ("TvShowsView", "TvShowsView.swift"),
    ("SavedView", "SavedView.swift"),
    ("DetailView", "DetailView.swift"),
    ("Components", "Components.swift"),
    ("AiSearchView", "AiSearchView.swift"),
]

BUNDLE_ID = "com.kiran.movie.iosApp"
PRODUCT_NAME = "iosApp"
SWIFT_VERSION = "5.0"
DEPLOYMENT_TARGET = "17.0"

# KMM framework script
GRADLE_ROOT = "${SRCROOT}/.."
KMM_SCRIPT = f"""
set -e
cd "{GRADLE_ROOT}"
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework "$BUILT_PRODUCTS_DIR/$PRODUCT_NAME.app/Frameworks/"
"""

pbxproj_content = f"""// !$*UTF8*$!
{{
	archiveVersion = 1;
	classes = {{
	}};
	objectVersion = 56;
	objects = {{

/* Begin PBXBuildFile section */
{"".join([f'''		{IDs[n+"_bf"]} /* {f} in Sources */ = {{isa = PBXBuildFile; fileRef = {IDs[n]}; }};
''' for n, f in SWIFT_FILES])}/* End PBXBuildFile section */

/* Begin PBXFileReference section */
		{IDs["product"]} /* {PRODUCT_NAME}.app */ = {{isa = PBXFileReference; explicitFileType = wrapper.application; includeInIndex = 0; path = "{PRODUCT_NAME}.app"; sourceTree = BUILT_PRODUCTS_DIR; }};
{"".join([f'''		{IDs[n]} /* {f} */ = {{isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = "{f}"; sourceTree = "<group>"; }};
''' for n, f in SWIFT_FILES])}		{IDs["infoPlist"]} /* Info.plist */ = {{isa = PBXFileReference; lastKnownFileType = text.plist.xml; path = Info.plist; sourceTree = "<group>"; }};
/* End PBXFileReference section */

/* Begin PBXFrameworksBuildPhase section */
		{IDs["frameworksBuildPhase"]} /* Frameworks */ = {{
			isa = PBXFrameworksBuildPhase;
			buildActionMask = 2147483647;
			files = (
			);
			runOnlyForDeploymentPostprocessing = 0;
		}};
/* End PBXFrameworksBuildPhase section */

/* Begin PBXGroup section */
		{IDs["mainGroup"]} = {{
			isa = PBXGroup;
			children = (
				{IDs["sourcesGroup"]},
				{IDs["frameworksGroup"]},
				{IDs["productsGroup"]},
			);
			sourceTree = "<group>";
		}};
		{IDs["sourcesGroup"]} /* {PRODUCT_NAME} */ = {{
			isa = PBXGroup;
			children = (
{"".join([f'''				{IDs[n]},
''' for n, f in SWIFT_FILES])}				{IDs["infoPlist"]},
			);
			path = {PRODUCT_NAME};
			sourceTree = "<group>";
		}};
		{IDs["productsGroup"]} /* Products */ = {{
			isa = PBXGroup;
			children = (
				{IDs["product"]},
			);
			name = Products;
			sourceTree = "<group>";
		}};
		{IDs["frameworksGroup"]} /* Frameworks */ = {{
			isa = PBXGroup;
			children = (
			);
			name = Frameworks;
			sourceTree = "<group>";
		}};
/* End PBXGroup section */

/* Begin PBXNativeTarget section */
		{IDs["target"]} /* {PRODUCT_NAME} */ = {{
			isa = PBXNativeTarget;
			buildConfigurationList = {IDs["targetBuildConfigList"]};
			buildPhases = (
				{IDs["scriptBuildPhase"]},
				{IDs["sourcesBuildPhase"]},
				{IDs["resourcesBuildPhase"]},
				{IDs["frameworksBuildPhase"]},
			);
			buildRules = (
			);
			dependencies = (
			);
			name = {PRODUCT_NAME};
			productName = {PRODUCT_NAME};
			productReference = {IDs["product"]};
			productType = "com.apple.product-type.application";
		}};
/* End PBXNativeTarget section */

/* Begin PBXProject section */
		{IDs["project"]} /* Project object */ = {{
			isa = PBXProject;
			attributes = {{
				BuildIndependentTargetsInParallel = 1;
				LastSwiftUpdateCheck = 1500;
				LastUpgradeCheck = 1500;
				TargetAttributes = {{
					{IDs["target"]} = {{
						CreatedOnToolsVersion = 15.0;
					}};
				}};
			}};
			buildConfigurationList = {IDs["buildConfigList"]};
			compatibilityVersion = "Xcode 14.0";
			developmentRegion = en;
			hasScannedForEncodings = 0;
			knownRegions = (
				en,
				Base,
			);
			mainGroup = {IDs["mainGroup"]};
			productRefGroup = {IDs["productsGroup"]};
			projectDirPath = "";
			projectRoot = "";
			targets = (
				{IDs["target"]},
			);
		}};
/* End PBXProject section */

/* Begin PBXResourcesBuildPhase section */
		{IDs["resourcesBuildPhase"]} /* Resources */ = {{
			isa = PBXResourcesBuildPhase;
			buildActionMask = 2147483647;
			files = (
			);
			runOnlyForDeploymentPostprocessing = 0;
		}};
/* End PBXResourcesBuildPhase section */

/* Begin PBXShellScriptBuildPhase section */
		{IDs["scriptBuildPhase"]} /* Build KMM Framework */ = {{
			isa = PBXShellScriptBuildPhase;
			buildActionMask = 2147483647;
			files = (
			);
			inputFileListPaths = (
			);
			inputPaths = (
			);
			name = "Build KMM Framework";
			outputFileListPaths = (
			);
			outputPaths = (
			);
			runOnlyForDeploymentPostprocessing = 0;
			shellPath = /bin/sh;
			shellScript = "set -e\\ncd \\"$SRCROOT/..\\"\\n./gradlew :shared:linkDebugFrameworkIosSimulatorArm64\\nmkdir -p \\"$BUILT_PRODUCTS_DIR/$PRODUCT_NAME.app/Frameworks\\"\\ncp -R shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework \\"$BUILT_PRODUCTS_DIR/$PRODUCT_NAME.app/Frameworks/\\"\\n/usr/bin/codesign --force --sign - --timestamp=none --generate-entitlement-der \\"$BUILT_PRODUCTS_DIR/$PRODUCT_NAME.app/Frameworks/shared.framework\\"\\n";
		}};
/* End PBXShellScriptBuildPhase section */

/* Begin PBXSourcesBuildPhase section */
		{IDs["sourcesBuildPhase"]} /* Sources */ = {{
			isa = PBXSourcesBuildPhase;
			buildActionMask = 2147483647;
			files = (
{"".join([f'''				{IDs[n+"_bf"]} /* {f} in Sources */,
''' for n, f in SWIFT_FILES])}			);
			runOnlyForDeploymentPostprocessing = 0;
		}};
/* End PBXSourcesBuildPhase section */

/* Begin XCBuildConfiguration section */
		{IDs["debugConfig"]} /* Debug */ = {{
			isa = XCBuildConfiguration;
			buildSettings = {{
				ALWAYS_SEARCH_USER_PATHS = NO;
				ASSETCATALOG_COMPILER_GENERATE_SWIFT_ASSET_SYMBOL_EXTENSIONS = YES;
				CLANG_ANALYZER_NONNULL = YES;
				CLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;
				CLANG_CXX_LANGUAGE_STANDARD = "gnu++20";
				CLANG_ENABLE_MODULES = YES;
				CLANG_ENABLE_OBJC_ARC = YES;
				CLANG_ENABLE_OBJC_WEAK = YES;
				CLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;
				CLANG_WARN_BOOL_CONVERSION = YES;
				CLANG_WARN_COMMA = YES;
				CLANG_WARN_CONSTANT_CONVERSION = YES;
				CLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;
				CLANG_WARN_DIRECT_OBJC_ISA_USAGE = YES_ERROR;
				CLANG_WARN_DOCUMENTATION_COMMENTS = YES;
				CLANG_WARN_EMPTY_BODY = YES;
				CLANG_WARN_ENUM_CONVERSION = YES;
				CLANG_WARN_INFINITE_RECURSION = YES;
				CLANG_WARN_INT_CONVERSION = YES;
				CLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;
				CLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;
				CLANG_WARN_OBJC_LITERAL_CONVERSION = YES;
				CLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;
				CLANG_WARN_QUOTED_INCLUDE_IN_FRAMEWORK_HEADER = YES;
				CLANG_WARN_RANGE_LOOP_ANALYSIS = YES;
				CLANG_WARN_STRICT_PROTOTYPES = YES;
				CLANG_WARN_SUSPICIOUS_MOVE = YES;
				CLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;
				CLANG_WARN_UNREACHABLE_CODE = YES;
				CLANG_WARN__DUPLICATE_METHOD_MATCH = YES;
				COPY_PHASE_STRIP = NO;
				DEBUG_INFORMATION_FORMAT = dwarf;
				ENABLE_STRICT_OBJC_MSGSEND = YES;
				ENABLE_TESTABILITY = YES;
				ENABLE_USER_SCRIPT_SANDBOXING = NO;
				GCC_C_LANGUAGE_STANDARD = gnu17;
				GCC_DYNAMIC_NO_PIC = NO;
				GCC_NO_COMMON_BLOCKS = YES;
				GCC_OPTIMIZATION_LEVEL = 0;
				GCC_PREPROCESSOR_DEFINITIONS = (
					"DEBUG=1",
					"$(inherited)",
				);
				GCC_WARN_64_TO_32_BIT_CONVERSION = YES;
				GCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;
				GCC_WARN_UNDECLARED_SELECTOR = YES;
				GCC_WARN_UNINITIALIZED_AUTOS = YES_AGGRESSIVE;
				GCC_WARN_UNUSED_FUNCTION = YES;
				GCC_WARN_UNUSED_VARIABLE = YES;
				IPHONEOS_DEPLOYMENT_TARGET = {DEPLOYMENT_TARGET};
				MTL_ENABLE_DEBUG_INFO = INCLUDE_SOURCE;
				MTL_FAST_MATH = YES;
				ONLY_ACTIVE_ARCH = YES;
				SDKROOT = iphoneos;
				SWIFT_ACTIVE_COMPILATION_CONDITIONS = DEBUG;
				SWIFT_OPTIMIZATION_LEVEL = "-Onone";
			}};
			name = Debug;
		}};
		{IDs["releaseConfig"]} /* Release */ = {{
			isa = XCBuildConfiguration;
			buildSettings = {{
				ALWAYS_SEARCH_USER_PATHS = NO;
				CLANG_ANALYZER_NONNULL = YES;
				CLANG_ENABLE_MODULES = YES;
				CLANG_ENABLE_OBJC_ARC = YES;
				CLANG_ENABLE_OBJC_WEAK = YES;
				GCC_C_LANGUAGE_STANDARD = gnu17;
				GCC_NO_COMMON_BLOCKS = YES;
				GCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;
				IPHONEOS_DEPLOYMENT_TARGET = {DEPLOYMENT_TARGET};
				MTL_FAST_MATH = YES;
				SDKROOT = iphoneos;
				SWIFT_COMPILATION_MODE = wholemodule;
				SWIFT_OPTIMIZATION_LEVEL = "-O";
				VALIDATE_PRODUCT = YES;
			}};
			name = Release;
		}};
		{IDs["targetDebugConfig"]} /* Debug */ = {{
			isa = XCBuildConfiguration;
			buildSettings = {{
				ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon;
				ASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME = AccentColor;
				CODE_SIGN_STYLE = Automatic;
				CURRENT_PROJECT_VERSION = 1;
				DEVELOPMENT_ASSET_PATHS = "";
				ENABLE_PREVIEWS = YES;
				FRAMEWORK_SEARCH_PATHS = (
					"$(inherited)",
					"$(PROJECT_DIR)/../shared/build/bin/iosSimulatorArm64/debugFramework",
				);
				LD_RUNPATH_SEARCH_PATHS = "@executable_path/Frameworks";
				GENERATE_INFOPLIST_FILE = NO;
				INFOPLIST_FILE = {PRODUCT_NAME}/Info.plist;
				IPHONEOS_DEPLOYMENT_TARGET = {DEPLOYMENT_TARGET};
				MARKETING_VERSION = 1.0;
				PRODUCT_BUNDLE_IDENTIFIER = "{BUNDLE_ID}";
				PRODUCT_NAME = "$(TARGET_NAME)";
				SWIFT_EMIT_LOC_STRINGS = YES;
				SWIFT_VERSION = {SWIFT_VERSION};
				TARGETED_DEVICE_FAMILY = "1,2";
			}};
			name = Debug;
		}};
		{IDs["targetReleaseConfig"]} /* Release */ = {{
			isa = XCBuildConfiguration;
			buildSettings = {{
				ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon;
				ASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME = AccentColor;
				CODE_SIGN_STYLE = Automatic;
				CURRENT_PROJECT_VERSION = 1;
				DEVELOPMENT_ASSET_PATHS = "";
				ENABLE_PREVIEWS = YES;
				FRAMEWORK_SEARCH_PATHS = (
					"$(inherited)",
					"$(PROJECT_DIR)/../shared/build/bin/iosSimulatorArm64/debugFramework",
				);
				LD_RUNPATH_SEARCH_PATHS = "@executable_path/Frameworks";
				GENERATE_INFOPLIST_FILE = NO;
				INFOPLIST_FILE = {PRODUCT_NAME}/Info.plist;
				IPHONEOS_DEPLOYMENT_TARGET = {DEPLOYMENT_TARGET};
				MARKETING_VERSION = 1.0;
				PRODUCT_BUNDLE_IDENTIFIER = "{BUNDLE_ID}";
				PRODUCT_NAME = "$(TARGET_NAME)";
				SWIFT_EMIT_LOC_STRINGS = YES;
				SWIFT_VERSION = {SWIFT_VERSION};
				TARGETED_DEVICE_FAMILY = "1,2";
			}};
			name = Release;
		}};
/* End XCBuildConfiguration section */

/* Begin XCConfigurationList section */
		{IDs["buildConfigList"]} /* Build configuration list for PBXProject "{PRODUCT_NAME}" */ = {{
			isa = XCConfigurationList;
			buildConfigurations = (
				{IDs["debugConfig"]},
				{IDs["releaseConfig"]},
			);
			defaultConfigurationIsVisible = 0;
			defaultConfigurationName = Release;
		}};
		{IDs["targetBuildConfigList"]} /* Build configuration list for PBXNativeTarget "{PRODUCT_NAME}" */ = {{
			isa = XCConfigurationList;
			buildConfigurations = (
				{IDs["targetDebugConfig"]},
				{IDs["targetReleaseConfig"]},
			);
			defaultConfigurationIsVisible = 0;
			defaultConfigurationName = Release;
		}};
/* End XCConfigurationList section */
	}};
	rootObject = {IDs["project"]};
}}
"""

os.makedirs(XCODEPROJ_DIR, exist_ok=True)
with open(PBXPROJ_PATH, "w") as f:
    f.write(pbxproj_content)

print(f"Generated {PBXPROJ_PATH}")
