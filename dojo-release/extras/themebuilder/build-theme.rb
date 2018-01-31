#!/usr/bin/ruby
#
# This script generates colorized images and CSS overrides for Web-CAT themes.
# It takes one argument, a .properties file containing information about the
# theme.
#
# --
# $Id: build-theme.rb,v 1.2 2010/02/10 19:25:46 aallowat Exp $

require 'rubygems'
require 'RMagick'
include Magick

#
# Default values and other constants.
#
DEFAULT_SECONDARY_TINT_COLOR = '#1A6CAB'
DEFAULT_TOOLTIP_BACKGROUND_COLOR = '#FFF8B8'
DEFAULT_TOOLTIP_BORDER_COLOR = '#979797'
CHECKBOX_WIDTH = 17

#
# Only apply tinting to these images in the master directory.
#
$images_to_tint = {
  'arrow-sprite.gif' => { :x => 0, :y => 0, :w => 36, :h => 10 },
  'arrow-sprite.png' => { :x => 0, :y => 0, :w => 36, :h => 10 },
  'button-active.png' => { },
  'button-enabled.png' => { },
  'dialog-close.png' => { },
  'dialog-titlebar.png' => { },
  'progress-bar-full.png' => { :secondary => true },
  'rounded-icons-sprite.png' => { :x => 0, :y => 0, :w => 75, :h => 30 },
  'slider-horiz-full.png' => { :secondary => true },
  'slider-vert-full.png' => { :secondary => true },
  'titlebar.png' => { },
  'titlebar-active.png' => { },
  'titlebar-hover.png' => { }
}

#
# These are images that we want to ignore (that is, neither tint nor
# copy). They are used for parts, to construct other larger images.
#
$images_to_ignore = {
  'checkbox-check.png' => true,
  'checkbox-enabled.png' => true,
  'checkbox-selected.png' => true,
  'radio-dot.png' => true,
  'radio-enabled.png' => true,
  'radio-selected.png' => true,
  'slider-enabled.png' => true,
  'slider-dot.png' => true,
  'slider-focus-ring.png' => true
}

#
# Convenient additions to the Magick::Pixel and Magick::Image classes.
#
module Magick
  class Pixel
    # ---------------------------------------------------------------
    #
    # Performs midtone tinting of the source color with a tint color at the
    # specified strength.
    #
    def tint(tint_color, strength)
      tint_intensity = tint_color.intensity.to_f / QuantumRange
      tr = tint_color.red.to_f / QuantumRange
      tg = tint_color.green.to_f / QuantumRange
      tb = tint_color.blue.to_f / QuantumRange
      cv_r = strength * tr / 100 - tint_intensity
      cv_g = strength * tg / 100 - tint_intensity
      cv_b = strength * tb / 100 - tint_intensity

      sr = self.red.to_f / QuantumRange
      sg = self.green.to_f / QuantumRange
      sb = self.blue.to_f / QuantumRange

      w = sr - 0.5
      dr = sr + cv_r * (1 - 4 * w * w)
      w = sg - 0.5
      dg = sg + cv_g * (1 - 4 * w * w)
      w = sb - 0.5
      db = sb + cv_b * (1 - 4 * w * w)

      dr = ([[dr, 0].max, 1].min * QuantumRange).to_i
      dg = ([[dg, 0].max, 1].min * QuantumRange).to_i
      db = ([[db, 0].max, 1].min * QuantumRange).to_i

      Pixel.new(dr, dg, db, self.opacity)
    end

    # ---------------------------------------------------------------
    #
    # Creates a copy of the color with the specified alpha value.
    #
    def color_with_alpha(alpha)
      Pixel.new(self.red, self.green, self.blue, alpha * QuantumRange)
    end

    # ---------------------------------------------------------------
    #
    # Shorthand method for converting an RMagick color into an HTML hex string.
    #
    def to_html
      self.to_color(AllCompliance, true, 8, true)
    end
  end


  class Image
    # ---------------------------------------------------------------
    #
    # Gets the color at a corner or edge of the image, using a gravity
    # constant to determine where it should be accessed.
    #
    def pixel_color_grav(gravity)
      x = case gravity
        when NorthWestGravity, WestGravity, SouthWestGravity then 0
        when NorthGravity, CenterGravity, SouthGravity       then self.columns / 2
        when NorthEastGravity, EastGravity, SouthEastGravity then self.columns - 1
      end
      y = case gravity
        when NorthWestGravity, NorthGravity, NorthEastGravity then 0
        when WestGravity, CenterGravity, EastGravity          then self.rows / 2
        when SouthWestGravity, SouthGravity, SouthEastGravity then self.rows - 1
      end
      self.pixel_color(x, y)
    end

    # ---------------------------------------------------------------
    #
    # Performs "map" over each pixel in the image, returning a new image with
    # the result. The block given to this function is passed an array of four
    # color values in [r,g,b,a] order, each ranging from 0 to 1.
    #
    def map_colors
      self.dup.map_colors!
    end

    # ---------------------------------------------------------------
    #
    # Performs "map" over each pixel in the image, modifying the image directly.
    # The block given to this function is passed an array of four color values
    # in [r,g,b,a] order, each ranging from 0 to 1.
    #
    def map_colors!(bounds=nil)
      bounds = bounds || { }
      rows = self.rows
      cols = self.columns

      left = bounds[:x] || 0
      top = bounds[:y] || 0
      width = bounds[:w] || cols
      height = bounds[:h] || rows

      (top ... top + height).each do |y|
        pixels = self.get_pixels(left, y, width, 1)

        pixels.map! do |c|
          ca = [c.red, c.green, c.blue, c.opacity]
          ca.map! { |v| v.to_f / QuantumRange }
          ca = yield ca
          ca.map! { |v| ([[v, 0].max, 1].min * QuantumRange).to_i }
          Pixel.new(*ca)
        end

        self.store_pixels left, y, width, 1, pixels
      end
      self
    end

    # ---------------------------------------------------------------
    #
    # Returns a translucent copy of the image by adjusting the alpha level of
    # its pixels by the specified opacity.
    #
    def make_translucent(opacity)
      self.dup.make_translucent! opacity
    end

    # ---------------------------------------------------------------
    #
    # Makes the image translucent by adjusting the alpha level of its pixels
    # by the specified opacity.
    #
    def make_translucent!(opacity)
      self.map_colors! nil do |p|
        p[3] = 1.0 - (1.0 - p[3]) * opacity
        p
      end
      self
    end

    # ---------------------------------------------------------------
    #
    # Performs midtone tinting of the colors in an image with a tint color at
    # the specified strength.
    #
    def tint!(tint_color, strength, bounds=nil)
      tint_intensity = tint_color.intensity.to_f / QuantumRange
      tr = tint_color.red.to_f / QuantumRange
      tg = tint_color.green.to_f / QuantumRange
      tb = tint_color.blue.to_f / QuantumRange
      cv_r = strength * tr / 100 - tint_intensity
      cv_g = strength * tg / 100 - tint_intensity
      cv_b = strength * tb / 100 - tint_intensity

      self.map_colors! bounds do |p|
        w = p[0] - 0.5
        p[0] = p[0] + cv_r * (1 - 4 * w * w)
        w = p[1] - 0.5
        p[1] = p[1] + cv_g * (1 - 4 * w * w)
        w = p[2] - 0.5
        p[2] = p[2] + cv_b * (1 - 4 * w * w)
        p
      end
      self
    end

    # ---------------------------------------------------------------
    #
    # Performs midtone tinting of the colors in an image with a tint color at
    # the specified strength.
    #
    def tint(tint_color, strength, bounds=nil)
      self.dup.tint! tint_color, strength, bounds
    end

  end
end


# ---------------------------------------------------------------
#
# Loads properties of the form "key=value" into a hash. Lines starting
# with # or = are ignored.
#
def load_properties(properties_filename)
  properties = {}
  File.open(properties_filename, 'r') do |properties_file|
    properties_file.read.each_line do |line|
      line.strip!
      if line[0] != ?# and line[0] != ?=
        i = line.index('=')
        if i
          key = line[0..i - 1].strip
          value = line[i + 1..-1].strip
        else
          key = line
          value = ''
        end

        if value == 'true'
          value = true
        elsif value == 'false'
          value = false
        end

        properties[key] = value
      end
    end
  end
  properties
end


# ---------------------------------------------------------------
#
# Shorthand method for creating an RMagick color from an HTML color string
# or from RGBA values (R, G, B between 0-255, A between 0 and 1).
#
def new_color(*args)
  if args.length == 1
    Pixel.from_color(args[0])
  else
    red = args[0] / 255.0 * QuantumRange
    green = args[1] / 255.0 * QuantumRange
    blue = args[2] / 255.0 * QuantumRange
    opacity = args[3] * QuantumRange
    Pixel.new(red, green, blue, opacity)
  end
end


# ---------------------------------------------------------------
#
# Generates the color hash that will be used by this style, as determined from
# parameters in the properties file.
#
def generate_style_colors
  colors = { }
  color_for_tint = new_color($properties['tintColor'])
  secondary_tint = new_color($properties['secondaryTintColor'] || DEFAULT_SECONDARY_TINT_COLOR)

  button_end_color = Image.read("#{$master_dir}/button-enabled.png").first.pixel_color_grav(SouthGravity)
  button_active_end_color = Image.read("#{$master_dir}/button-active.png").first.pixel_color_grav(NorthGravity)
  dialog_titlebar_end_color = Image.read("#{$master_dir}/dialog-titlebar.png").first.pixel_color_grav(SouthGravity)
  slider_light_color = Image.read("#{$master_dir}/slider-horiz-full.png").first.pixel_color_grav(NorthGravity)
  slider_dark_color = Image.read("#{$master_dir}/slider-horiz-full.png").first.pixel_color_grav(SouthGravity)
  titlebar_end_color = Image.read("#{$master_dir}/titlebar.png").first.pixel_color_grav(SouthGravity)
  titlebar_active_end_color = Image.read("#{$master_dir}/titlebar-active.png").first.pixel_color_grav(SouthGravity)
  titlebar_hover_end_color = Image.read("#{$master_dir}/titlebar-hover.png").first.pixel_color_grav(SouthGravity)

  if $properties['dark']

    # TODO: finish support for dark themes
    colors['primary-background-color'] = new_color('#202020')
    colors['primary-text-color'] = new_color('#FFFFFF')

  else

    colors['primary-background-color'] = new_color('#FFFFFF')
    colors['primary-text-color'] = new_color('#000000')

    colors['autocomplete-highlight-color'] = secondary_tint.color_with_alpha(0.5)

    colors['button-background-color'] = button_end_color.tint(color_for_tint, 100)
    colors['button-border-color'] = new_color('#666666').tint(color_for_tint, 100)
    colors['button-active-background-color'] = button_active_end_color.tint(color_for_tint, 100)
    colors['button-active-border-color'] = new_color('#666666').tint(color_for_tint, 100)
    colors['button-disabled-background-color'] = button_end_color
    colors['button-disabled-border-color'] = new_color('#A3A3A3')
    colors['button-disabled-text-color'] = new_color('#7F7F7F')
    colors['button-disabled-text-shadow-color'] = new_color('#F5F5F5')
    colors['button-hover-background-color'] = button_end_color.tint(color_for_tint, 100)
    colors['button-hover-border-color'] = new_color('#BBBBBB').tint(color_for_tint, 100)
    colors['button-hover-border-bottom-color'] = new_color('#999999').tint(color_for_tint, 100)
    colors['button-text-color'] = new_color('#202020').tint(color_for_tint, 100)
    colors['button-text-shadow-color'] = new_color('#EBEBEB')
    colors['checkbox-hover-background-color'] = secondary_tint.color_with_alpha(0.5)
    colors['combobutton-separator-color'] = new_color('#DBDBDB').tint(color_for_tint, 100)
    colors['combobutton-disabled-separator-color'] = new_color('#DBDBDB')

    colors['calendar-adjacent-month-date-label-color'] = new_color('#8B8B8B')
    colors['calendar-border-color'] = new_color('#666666').tint(color_for_tint, 100)
    colors['calendar-dayofweek-text-color'] = new_color('#404040').tint(color_for_tint, 100)
    colors['calendar-disabled-background-color'] = new_color('#BBBBBC')
    colors['calendar-current-month-date-border-color'] = new_color('#E3E3E3').tint(color_for_tint, 100)
    colors['calendar-hover-date-background-color'] = new_color('#EEEEEE').tint(secondary_tint, 100)
    colors['calendar-year-background-color'] = new_color('#D0D0D0').tint(color_for_tint, 100)
    colors['calendar-year-selected-border-color'] = new_color('#666666').tint(color_for_tint, 100)
    colors['calendar-year-prevnext-border-color'] = new_color('#666666').tint(color_for_tint, 100)

    colors['container-border-color'] = new_color('#666666').tint(color_for_tint, 100)

    colors['dialog-background-color'] = dialog_titlebar_end_color.tint(color_for_tint, 100)
    colors['dialog-border-color'] = new_color('#666666').tint(color_for_tint, 100)
    colors['dialog-box-shadow-color'] = new_color('#000000')
    colors['dialog-underlay-color'] = new_color('#222222')

    colors['dnd-avatar-can-drop-color'] = new_color('#07930E')
    colors['dnd-avatar-cannot-drop-color'] = new_color('#FF0000')
    colors['dnd-insert-point-color'] = new_color('#333333')

    colors['focus-label-outline-color'] = new_color('#666666')
    colors['focus-ring-color'] = secondary_tint.color_with_alpha(0.95)

    colors['input-border-color'] = new_color('#999999').tint(color_for_tint, 100)
    colors['input-disabled-border-color'] = new_color('#999999')
    colors['input-disabled-text-color'] = new_color('#888888')
    colors['input-invalid-background-color'] = new_color('#F9F7BA')

    colors['menu-background-color'] = new_color('#F4F4F4')
    colors['menu-border-color'] = new_color('#ACACAC')
    colors['menu-separator-dark-color'] = new_color('#9B9B9B')
    colors['menu-separator-light-color'] = new_color('#E8E8E8')
    colors['menu-text-color'] = colors['primary-text-color']
    colors['menu-text-shadow-color'] = colors['primary-background-color']
    colors['menubar-background-color'] = titlebar_end_color.tint(color_for_tint, 100)
    colors['menubar-text-color'] = colors['primary-text-color']

    colors['popup-box-shadow-color'] = new_color('#515151')

    colors['progress-bar-empty-background-color'] = new_color('#F0F0F0')
    colors['progress-bar-empty-border-color'] = new_color('#C8C8C8')
    colors['progress-bar-indeterminate-border-color'] = new_color('#8EA8B8')
    colors['progress-bar-text-color'] = new_color('#697A8B')
    colors['progress-bar-tile-background-color'] = secondary_tint

    colors['selected-item-background-color'] = secondary_tint
    colors['selected-item-text-color'] = new_color('#FFFFFF')

    colors['slider-horiz-progress-background-color'] = slider_dark_color.tint(secondary_tint, 100)
    colors['slider-horiz-progress-border-color'] = slider_dark_color.tint(secondary_tint, 85)
    colors['slider-horiz-remainder-background-color'] = new_color('#CBCBCB')
    colors['slider-horiz-remainder-border-color'] = new_color('#919191')
    colors['slider-vert-progress-background-color'] = slider_dark_color.tint(secondary_tint, 100)
    colors['slider-vert-progress-border-color'] = slider_dark_color.tint(secondary_tint, 85)
    colors['slider-vert-remainder-background-color'] = new_color('#B3B3B3')
    colors['slider-vert-remainder-border-color'] = new_color('#B3B3B3')

    colors['splitter-background-color'] = new_color('#F0F0F0').tint(color_for_tint, 100)

    colors['tab-background-color'] = new_color('#F0F0F0').tint(color_for_tint, 100)
    colors['tab-glow-color'] = new_color(134, 163, 180, 0.45) #TINT

    colors['titlebar-active-background-color'] = titlebar_active_end_color.tint(color_for_tint, 100)
    colors['titlebar-hover-background-color'] = titlebar_hover_end_color.tint(color_for_tint, 100)
    colors['titlebar-background-color'] = titlebar_end_color.tint(color_for_tint, 100)

    colors['toolbar-button-background-color'] = new_color('#FFFFFF') #TINT

    colors['tooltip-background-color'] = new_color($properties['tooltipBackgroundColor'] || DEFAULT_TOOLTIP_BACKGROUND_COLOR)
    colors['tooltip-border-color'] = new_color($properties['tooltipBorderColor'] || DEFAULT_TOOLTIP_BORDER_COLOR)

  end

  colors
end


# ---------------------------------------------------------------
#
# Draw an arrow as an open triangle with the three points passed into the
# function. Used for the tooltip connectors.
#
def draw_arrow(*points)
  border_color = $properties['tooltipBorderColor'] || DEFAULT_TOOLTIP_BORDER_COLOR
  fill_color = $properties['tooltipBackgroundColor'] || DEFAULT_TOOLTIP_BACKGROUND_COLOR

  gc = Draw.new
  gc.stroke border_color
  gc.fill fill_color + 'F2' # 0.95 alpha
  gc.polyline points[0][0], points[0][1], points[1][0], points[1][1],
              points[2][0], points[2][1]

  img = Image.new(16, 14) do
    self.background_color = 'none'
  end

  gc.draw img
  img
end


# ---------------------------------------------------------------
#
# Generate the images for the theme.
#
def generate_images
  button_tint = Pixel.from_color($properties['tintColor'])
  secondary_tint = Pixel.from_color($properties['secondaryTintColor'])

  Dir.glob("#{$master_dir}/*.{png,gif}") do |f|
    fname = File.basename(f)
    unless $images_to_ignore[fname]
      tint_bounds = $images_to_tint[fname]
      if tint_bounds
        # If the image is in the $images_to_tint hash, then we need to tint it.
        # The value in that hash is either 'true', which means tint the whole
        # thing, or a hash with :x, :y, :w, and :h keys defining the region to
        # tint.
        color_to_use = tint_bounds[:secondary] ? secondary_tint : button_tint

        image = Image.read(f).first
        image.tint! color_to_use, 100, tint_bounds
        image.write "images/#{fname}"
      else
        # Just copy the image without tinting it.
        FileUtils.cp "#{$master_dir}/#{fname}", "images/#{fname}"
      end
    end
  end

  # Create copies of other images we need.
  FileUtils.cp "#{$master_dir}/button-enabled.png", 'images/button-disabled.png'

  # Generate other images we need.
  generate_tooltip_connectors
  generate_checkbox_sprite_image
  generate_slider_thumb_images
  generate_indeterminate_progress_bar

end


# ---------------------------------------------------------------
#
# Generate the tooltip connector images.
#
def generate_tooltip_connectors
  arrow = draw_arrow([1, 14], [7, 4], [13, 14])
  arrow.write 'images/tooltip-connector-up.png'

  arrow = draw_arrow([1, 0], [7, 10], [13, 0])
  arrow.write 'images/tooltip-connector-down.png'

  arrow = draw_arrow([15, 0], [5, 6], [15, 12])
  arrow.write 'images/tooltip-connector-left.png'

  arrow = draw_arrow([0, 0], [10, 6], [0, 12])
  arrow.write 'images/tooltip-connector-right.png'
end


# ---------------------------------------------------------------
#
# Generate the checkbox and radio button sprite image.
#
def generate_checkbox_sprite_image
  button_tint = Pixel.from_color($properties['tintColor'])

  checkbox_enabled = Image.read("#{$master_dir}/checkbox-enabled.png").first
  checkbox_tinted = Image.read("#{$master_dir}/checkbox-selected.png").first.tint(button_tint, 100)
  checkbox_check = Image.read("#{$master_dir}/checkbox-check.png").first
  checkbox_check_tinted = checkbox_check.tint(button_tint, 100)
  checkbox_disabled = checkbox_enabled.make_translucent 0.6
  checkbox_check_disabled = checkbox_check.make_translucent 0.6

  radio_enabled = Image.read("#{$master_dir}/radio-enabled.png").first
  radio_tinted = Image.read("#{$master_dir}/radio-selected.png").first.tint(button_tint, 100)
  radio_check = Image.read("#{$master_dir}/radio-dot.png").first
  radio_check_tinted = radio_check.tint(button_tint, 100)
  radio_disabled = radio_enabled.make_translucent 0.6
  radio_check_disabled = radio_check.make_translucent 0.6

  img = Image.new(170, 17) do
    self.background_color = 'none'
  end

  img.composite! checkbox_tinted, 0, 0, OverCompositeOp
  img.composite! checkbox_check_tinted, 0, 0, OverCompositeOp
  img.composite! checkbox_tinted, CHECKBOX_WIDTH * 1, 0, OverCompositeOp
  img.composite! checkbox_tinted, CHECKBOX_WIDTH * 2, 0, OverCompositeOp
  img.composite! checkbox_disabled, CHECKBOX_WIDTH * 3, 0, OverCompositeOp
  img.composite! checkbox_disabled, CHECKBOX_WIDTH * 4, 0, OverCompositeOp
  img.composite! checkbox_check_disabled, CHECKBOX_WIDTH * 4, 0, OverCompositeOp

  img.composite! radio_tinted, CHECKBOX_WIDTH * 5, 0, OverCompositeOp
  img.composite! radio_check_tinted, CHECKBOX_WIDTH * 5, 0, OverCompositeOp
  img.composite! radio_tinted, CHECKBOX_WIDTH * 6, 0, OverCompositeOp
  img.composite! radio_tinted, CHECKBOX_WIDTH * 7, 0, OverCompositeOp
  img.composite! radio_disabled, CHECKBOX_WIDTH * 8, 0, OverCompositeOp
  img.composite! radio_disabled, CHECKBOX_WIDTH * 9, 0, OverCompositeOp
  img.composite! radio_check_disabled, CHECKBOX_WIDTH * 9, 0, OverCompositeOp

  img.write 'images/checkbox-sprite.png'
end


# ---------------------------------------------------------------
#
# Generates the images used for the slider thumbs.
#
def generate_slider_thumb_images
  button_tint = Pixel.from_color($properties['tintColor'])
  secondary_tint = new_color($properties['secondaryTintColor'] || DEFAULT_SECONDARY_TINT_COLOR)

  slider_knob = Image.read("#{$master_dir}/slider-enabled.png").first.tint(button_tint, 100)
  slider_dot = Image.read("#{$master_dir}/slider-dot.png").first.tint(button_tint, 100)
  slider_focus_ring = Image.read("#{$master_dir}/slider-focus-ring.png").first

  img = Image.new(23, 23) do
    self.background_color = 'none'
  end

  img.composite! slider_knob, 0, 0, OverCompositeOp
  img.composite! slider_dot, 0, 0, OverCompositeOp

  img.write 'images/slider-thumb-enabled.png'

  img = Image.new(23, 23) do
    self.background_color = 'none'
  end

  img.composite! slider_focus_ring, 0, 0, OverCompositeOp
  img.composite! slider_knob, 0, 0, OverCompositeOp
  img.composite! slider_dot, 0, 0, OverCompositeOp

  img.write 'images/slider-thumb-focused.png'
end


# ---------------------------------------------------------------
#
# Generates the animated indeterminate progress bar.
#
def generate_indeterminate_progress_bar
  secondary_tint = new_color($properties['secondaryTintColor'] || DEFAULT_SECONDARY_TINT_COLOR)
  imgs = ImageList.new("#{$master_dir}/progress-bar-indeterminate.gif")

  imgs.each do |img|
    img.tint! secondary_tint, 100
  end

  imgs.write 'images/progress-bar-indeterminate.gif'
end


# ---------------------------------------------------------------
#
# Generate the CSS image and color overrides.
#
def generate_css_overrides
  css_content = ''
  File.open("#{$master_dir}/theme-overrides.css", 'r') do |css_file|
    css_content = css_file.read

    $style_colors.each do |key, color|
      if color.opacity != 0 and color.opacity != 1
        r = (color.red.to_f / QuantumRange * 255).to_i
        g = (color.green.to_f / QuantumRange * 255).to_i
        b = (color.blue.to_f / QuantumRange * 255).to_i
        a = (color.opacity.to_f / QuantumRange)
        color_string = "rgba(#{r}, #{g}, #{b}, #{a})"
      else
        color_string = color.to_html
      end
      css_content.gsub! /\$\{#{key}\}/, color_string
    end
  end

  File.open('dojo-overrides.css', 'w') do |out_file|
    out_file.write css_content
  end
end


# ---------------------------------------------------------------
#
# Main program.
#

$script_dir = Dir.pwd
$master_dir = "#{$script_dir}/master"

prop_file = File.expand_path(ARGV[0])

$properties = load_properties(prop_file)
$style_colors = generate_style_colors()

dir_name = File.dirname(prop_file)
Dir.chdir dir_name
puts "Generating theme: #{dir_name}"

FileUtils.mkdir_p 'images'

generate_images
generate_css_overrides

# End.
