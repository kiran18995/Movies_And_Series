require 'xcodeproj'
project = Xcodeproj::Project.open('iosApp.xcodeproj')
target = project.targets.first
group = project.main_group.find_subpath('iosApp', true)

# check if Assets.xcassets is already added
unless group.children.find { |c| c.path == 'Assets.xcassets' }
  file_ref = group.new_reference('Assets.xcassets')
  target.resources_build_phase.add_file_reference(file_ref)
end

target.build_configurations.each do |config|
  config.build_settings['ASSETCATALOG_COMPILER_APPICON_NAME'] = 'AppIcon'
end
project.save
