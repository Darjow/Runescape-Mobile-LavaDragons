package helper;

import logic.Restocking;
import logic.Wilderness;
import logic.WorldHopping;
import nodes.FightDragons;
import org.powbot.api.Tile;
import org.powbot.api.rt4.Combat;
import org.powbot.api.rt4.Movement;

public class Walker {
    public final static Tile[] fromDragonToSouth ={

            new Tile(3197, 3859, 0),
            new Tile(3195, 3861, 0),
            new Tile(3192, 3863, 0),
            new Tile(3189, 3865, 0),
            new Tile(3186, 3865, 0),
            new Tile(3184, 3864, 0),
            new Tile(3181, 3864, 0),
            new Tile(3178, 3864, 0),
            new Tile(3175, 3865, 0),
            new Tile(3172, 3865, 0),
            new Tile(3169, 3865, 0),
            new Tile(3166, 3865, 0),
            new Tile(3163, 3865, 0),
            new Tile(3160, 3865, 0),
            new Tile(3158, 3864, 0),
            new Tile(3158, 3861, 0),
            new Tile(3158, 3858, 0),
            new Tile(3161, 3855, 0),
            new Tile(3164, 3852, 0),
            new Tile(3167, 3849, 0),
            new Tile(3168, 3848, 0),
            new Tile(3168, 3845, 0),
            new Tile(3168, 3842, 0),
            new Tile(3168, 3839, 0),
            new Tile(3168, 3836, 0),
            new Tile(3168, 3833, 0),
            new Tile(3168, 3830, 0),
            new Tile(3167, 3827, 0),
            new Tile(3167, 3824, 0),
            new Tile(3167, 3821, 0),
            new Tile(3167, 3818, 0),
            new Tile(3167, 3815, 0),
            new Tile(3167, 3812, 0),
            new Tile(3167, 3809, 0),
            new Tile(3167, 3806, 0),
            new Tile(3168, 3803, 0),
            new Tile(3168, 3800, 0),
            new Tile(3168, 3797, 0),
            new Tile(3170, 3794, 0),
            new Tile(3170, 3791, 0),
            new Tile(3170, 3788, 0),
            new Tile(3171, 3785, 0),
            new Tile(3171, 3782, 0),
            new Tile(3174, 3779, 0),
            new Tile(3177, 3776, 0),
            new Tile(3180, 3773, 0),
            new Tile(3181, 3770, 0),
            new Tile(3182, 3767, 0),
            new Tile(3182, 3767, 0),
            new Tile(3182, 3764, 0),
            new Tile(3182, 3761, 0),
            new Tile(3184, 3758, 0),
            new Tile(3185, 3755, 0),
            new Tile(3185, 3752, 0),
            new Tile(3185, 3749, 0),
            new Tile(3187, 3746, 0),
            new Tile(3187, 3743, 0),
            new Tile(3187, 3740, 0),
            new Tile(3187, 3737, 0),
            new Tile(3187, 3734, 0),
            new Tile(3187, 3731, 0),
            new Tile(3187, 3728, 0),
            new Tile(3190, 3725, 0),
            new Tile(3193, 3722, 0)
    };

    public static final Tile[] fromCaveToDragon =  new Tile[]{
            new Tile(3206, 3683, 0),
            new Tile(3206, 3685, 0),
            new Tile(3206, 3687, 0),
            new Tile(3204, 3689, 0),
            new Tile(3204, 3691, 0),
            new Tile(3204, 3693, 0),
            new Tile(3204, 3695, 0),
            new Tile(3203, 3697, 0),
            new Tile(3202, 3699, 0),
            new Tile(3202, 3701, 0),
            new Tile(3202, 3703, 0),
            new Tile(3202, 3705, 0),
            new Tile(3200, 3707, 0),
            new Tile(3198, 3709, 0),
            new Tile(3196, 3711, 0),
            new Tile(3194, 3713, 0),
            new Tile(3192, 3715, 0),
            new Tile(3192, 3717, 0),
            new Tile(3192, 3719, 0),
            new Tile(3192, 3721, 0),
            new Tile(3192, 3723, 0),
            new Tile(3192, 3725, 0),
            new Tile(3191, 3727, 0),
            new Tile(3191, 3729, 0),
            new Tile(3191, 3731, 0),
            new Tile(3191, 3733, 0),
            new Tile(3190, 3735, 0),
            new Tile(3190, 3737, 0),
            new Tile(3190, 3739, 0),
            new Tile(3190, 3741, 0),
            new Tile(3188, 3743, 0),
            new Tile(3188, 3745, 0),
            new Tile(3188, 3747, 0),
            new Tile(3188, 3749, 0),
            new Tile(3187, 3751, 0),
            new Tile(3187, 3753, 0),
            new Tile(3187, 3755, 0),
            new Tile(3187, 3757, 0),
            new Tile(3187, 3759, 0),
            new Tile(3187, 3761, 0),
            new Tile(3185, 3763, 0),
            new Tile(3183, 3765, 0),
            new Tile(3181, 3767, 0),
            new Tile(3179, 3769, 0),
            new Tile(3178, 3770, 0),
            new Tile(3177, 3772, 0),
            new Tile(3175, 3774, 0),
            new Tile(3175, 3776, 0),
            new Tile(3175, 3778, 0),
            new Tile(3175, 3780, 0),
            new Tile(3174, 3782, 0),
            new Tile(3172, 3784, 0),
            new Tile(3170, 3786, 0),
            new Tile(3168, 3788, 0),
            new Tile(3168, 3790, 0),
            new Tile(3168, 3792, 0),
            new Tile(3168, 3794, 0),
            new Tile(3168, 3796, 0),
            new Tile(3168, 3798, 0),
            new Tile(3168, 3800, 0),
            new Tile(3168, 3802, 0),
            new Tile(3168, 3804, 0),
            new Tile(3168, 3806, 0),
            new Tile(3168, 3808, 0),
            new Tile(3168, 3810, 0),
            new Tile(3168, 3812, 0),
            new Tile(3168, 3814, 0),
            new Tile(3168, 3816, 0),
            new Tile(3167, 3818, 0),
            new Tile(3167, 3820, 0),
            new Tile(3167, 3822, 0),
            new Tile(3167, 3824, 0),
            new Tile(3167, 3826, 0),
            new Tile(3167, 3828, 0),
            new Tile(3167, 3830, 0),
            new Tile(3167, 3832, 0),
            new Tile(3167, 3834, 0),
            new Tile(3165, 3836, 0),
            new Tile(3164, 3838, 0),
            new Tile(3162, 3840, 0),
            new Tile(3161, 3842, 0),
            new Tile(3161, 3844, 0),
            new Tile(3161, 3846, 0),
            new Tile(3161, 3848, 0),
            new Tile(3161, 3850, 0),
            new Tile(3161, 3852, 0),
            new Tile(3161, 3854, 0),
            new Tile(3161, 3856, 0),
            new Tile(3161, 3858, 0),
            new Tile(3160, 3860, 0),
            new Tile(3158, 3862, 0),
            new Tile(3158, 3864, 0),
            new Tile(3159, 3865, 0),
            new Tile(3161, 3865, 0),
            new Tile(3163, 3865, 0),
            new Tile(3165, 3865, 0),
            new Tile(3167, 3865, 0),
            new Tile(3169, 3865, 0),
            new Tile(3171, 3865, 0),
            new Tile(3173, 3865, 0),
            new Tile(3175, 3865, 0),
            new Tile(3177, 3865, 0),
            new Tile(3179, 3864, 0),
            new Tile(3181, 3864, 0),
            new Tile(3183, 3864, 0),
            new Tile(3185, 3864, 0),
            new Tile(3186, 3865, 0),
            new Tile(3188, 3865, 0),
            new Tile(3190, 3865, 0),
            new Tile(3192, 3864, 0),
            new Tile(3194, 3862, 0),
            new Tile(3196, 3860, 0),
            new Tile(3198, 3859, 0),
            new Tile(3200, 3858, 0),

    };

    public static void wildernessWalker(Tile[] path, int minRun, boolean teleporting, int maxLevel) {
        boolean check = false;
        WorldHopping.isWorldOpen();

        if (Wilderness.getWildernessLevel() == 0 || Combat.wildernessLevel() == 0) {
            System.out.println("We can't be attacked");
        } else {
            if (teleporting && Wilderness.getWildernessLevel() <= maxLevel) {
                System.out.println("Teleport event received");
                check = Wilderness.teleport();
            } else if (!teleporting && Wilderness.isBeingPked()) {
                System.out.println("Change of plan .. We are being pked while not going to bank");
                System.out.println("Our new path is our banking path from dragons");
                path = fromDragonToSouth;
            }

            if (!check) {
                Movement.newTilePath(path).setRunMin(minRun).setRunMax(35).randomize(1, 2).traverse();

                if (Restocking.needHeal()) {
                    if (Restocking.hasFood()) {
                        Restocking.heal(true);
                    } else {
                        System.out.println("We are out of food");
                    }
                }
            } else {
                System.out.println("We are no longer retreating");
                FightDragons.retreating = false;
            }
        }
    }


}
