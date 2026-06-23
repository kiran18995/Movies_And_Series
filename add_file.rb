require 'xcodeproj'
project_path = 'iosApp/iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)
target = project.targets.first
group = project.main_group.find_subpath(File.join('iosApp'), true)
file_ref = group.new_reference('MovieWebView.swift')
target.add_file_references([file_ref])
project.save
