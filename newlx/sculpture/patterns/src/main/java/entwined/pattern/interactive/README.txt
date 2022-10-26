
// Per shrub interactivity - HSV
//
// Goal is to have some nice basic obvious effects which overlay all other effects on a per-shrub basis
// Thus, we want to expose parameters that are per-shrub --- which means a LOT of them!
// Note: it seems we don't really need parameters and we don't need to expose them, because the easy
// thing is to expose setters on this function, instead of register all these parameters with known values
// and look them up.
//
// Current experiments show the "hue set" interaction to be a great middle ground between something more blatent,
// and something that's easy to see. Brightness and saturation are kinda fun too - brightness more than saturation.
//
// Parameters: 'Hue' --> 0 to 360 of the hue for a shrub
//           'HueVal' --> the number of "degrees" the hue is squashed to (see above), which means 0 for off,
//                  160 is a good amount for "on"
//          'HueShift' -> we experimented with this but didn't go to production - on some patterns its impossible to see
//          'Brightness', 'Saturation' -> instead of using "set to value" we went with "multiply" because it allows
//             the pattern to come through better.
//
// Note: performance is rather important. For each cube, we need to access the current parameter for that cube,
// so the lookup into the set of paramters will happen a lot. Use classic arrays because the number of shrubs
// won't change. For that reason, this should probably change to arrays of floats or arrays of atomic integers.
//
// Sorry about reusing too much of the math code. Just getting something working. Feel free to clean
// up later.
//
// There is another module which keeps track of whether we're connected and can reset the parameters.
// Count on that unit to do that.
// Author: Brian Bulkowski 2021 brian@bulkowski.org





