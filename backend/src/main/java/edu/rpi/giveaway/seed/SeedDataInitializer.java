package edu.rpi.giveaway.seed;

import edu.rpi.giveaway.post.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Profile("!prod")
@ConditionalOnProperty(name = "giveaway.seed.enabled", havingValue = "true", matchIfMissing = true)
public class SeedDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataInitializer.class);

    private final PostService postService;
    private final PostRepository postRepository;

    public SeedDataInitializer(PostService postService, PostRepository postRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) {
        if (!postRepository.findAll().isEmpty()) {
            log.info("Posts already exist in repository. Skipping seed data initialization.");
            return;
        }

        log.info("Seeding initial 12 RPI campus giveaway posts...");
        Instant now = Instant.now();

        List<CreatePostRequest> seedRequests = List.of(
                new CreatePostRequest(
                        "IKEA Micke Desk - White",
                        "Great study desk with cable organizer and storage drawer. Moving out of Cary Hall, must pick up by Friday.",
                        Category.FURNITURE,
                        Condition.GOOD,
                        BigDecimal.ZERO,
                        "Cary Hall, Room 204",
                        now.plus(14, ChronoUnit.DAYS),
                        "/api/media/seed-desk.jpg",
                        "Alex Rivera",
                        "rivera@rpi.edu",
                        true
                ),
                new CreatePostRequest(
                        "Dell 27-inch 144Hz Gaming Monitor",
                        "Works perfectly, 1080p 144Hz display. Includes power cable and HDMI. Great for dorm gaming or second screen.",
                        Category.ELECTRONICS,
                        Condition.LIKE_NEW,
                        new BigDecimal("65.00"),
                        "BARH A-Tower Lobby",
                        now.plus(20, ChronoUnit.DAYS),
                        "/api/media/seed-monitor.jpg",
                        "Jordan Chen",
                        "+15185550101",
                        true
                ),
                new CreatePostRequest(
                        "Stewart Calculus: Early Transcendentals 9th Ed",
                        "Clean textbook used for MATH 1010 and 1020. No missing pages or highlighting. Free to anyone taking Calc I/II.",
                        Category.TEXTBOOKS,
                        Condition.GOOD,
                        BigDecimal.ZERO,
                        "Folsom Library 1st Floor",
                        now.plus(30, ChronoUnit.DAYS),
                        "/api/media/seed-calc-book.jpg",
                        "Taylor Smith",
                        "smitht9@rpi.edu",
                        true
                ),
                new CreatePostRequest(
                        "Keurig K-Mini Single Serve Coffee Maker",
                        "Matte black single-serve coffee maker. Descaled last week, super compact for tiny dorm counters.",
                        Category.KITCHEN,
                        Condition.LIKE_NEW,
                        new BigDecimal("15.00"),
                        "Blitman Commons Lounge",
                        now.plus(18, ChronoUnit.DAYS),
                        "/api/media/seed-keurig.jpg",
                        "Morgan Lee",
                        "+15185550102",
                        true
                ),
                new CreatePostRequest(
                        "Twin XL Memory Foam Mattress Topper (3-inch)",
                        "Clean 3-inch high density gel memory foam topper. Makes RPI dorm beds actually comfortable. Has washable cover.",
                        Category.DORM_ESSENTIALS,
                        Condition.LIKE_NEW,
                        BigDecimal.ZERO,
                        "Barton Hall Front Entrance",
                        now.plus(12, ChronoUnit.DAYS),
                        "/api/media/seed-mattress-topper.jpg",
                        "Sam Patel",
                        "patels3@rpi.edu",
                        true
                ),
                new CreatePostRequest(
                        "Trek FX 2 Disc Hybrid Commuter Bike",
                        "Medium frame commuter bike. Shifts smoothly, disc brakes in great shape. Includes U-lock and helmet.",
                        Category.BIKES,
                        Condition.GOOD,
                        new BigDecimal("80.00"),
                        "ECAV Bike Racks",
                        now.plus(25, ChronoUnit.DAYS),
                        "/api/media/seed-bike.jpg",
                        "Casey Miller",
                        "+15185550103",
                        true
                ),
                new CreatePostRequest(
                        "Ergonomic Mesh Office Chair",
                        "Adjustable armrests and lumbar support. Swivel base rolls easily. Leaving Troy after graduation.",
                        Category.FURNITURE,
                        Condition.GOOD,
                        new BigDecimal("25.00"),
                        "Quadrangle (Pardee Hall)",
                        now.plus(15, ChronoUnit.DAYS),
                        "/api/media/seed-chair.jpg",
                        "Devon Wu",
                        "wud4@rpi.edu",
                        true
                ),
                new CreatePostRequest(
                        "TI-84 Plus CE Color Graphing Calculator",
                        "Rechargeable battery holds full charge. Includes USB charging cord. Perfect for engineering coursework.",
                        Category.ELECTRONICS,
                        Condition.LIKE_NEW,
                        new BigDecimal("40.00"),
                        "DCC Great Hall",
                        now.plus(21, ChronoUnit.DAYS),
                        "/api/media/seed-ti84.jpg",
                        "Riley Jones",
                        "+15185550104",
                        true
                ),
                new CreatePostRequest(
                        "Operating Systems: Three Easy Pieces (OSTEP)",
                        "Hardcover edition of OSTEP used for CSCI 4210. Excellent reference guide for computer systems.",
                        Category.TEXTBOOKS,
                        Condition.LIKE_NEW,
                        BigDecimal.ZERO,
                        "Amos Eaton Lounge",
                        now.plus(28, ChronoUnit.DAYS),
                        "/api/media/seed-ostep.jpg",
                        "Jamie Zhang",
                        "zhangj12@rpi.edu",
                        true
                ),
                new CreatePostRequest(
                        "Compact Microwave 700W",
                        "Whirlpool small microwave, clean inside and outside. Fully functional turntable and keypad.",
                        Category.KITCHEN,
                        Condition.GOOD,
                        BigDecimal.ZERO,
                        "Davison Hall 3rd Floor",
                        now.plus(14, ChronoUnit.DAYS),
                        "/api/media/seed-microwave.jpg",
                        "Avery Davis",
                        "davisa5@rpi.edu",
                        true
                ),
                new CreatePostRequest(
                        "RPI Engineers Hockey Red Hoodie (Size L)",
                        "Official RPI hockey heavyweight hoodie. Super warm for Houston Field House games. Worn twice.",
                        Category.CLOTHING,
                        Condition.LIKE_NEW,
                        BigDecimal.ZERO,
                        "Student Union Rathskeller",
                        now.plus(24, ChronoUnit.DAYS),
                        "/api/media/seed-hoodie.jpg",
                        "Chris Martin",
                        "+15185550105",
                        true
                ),
                new CreatePostRequest(
                        "Honeywell QuietSet Tower Fan",
                        "5 speed oscillating tower fan with remote. Quiet operation, essential for August in freshmen dorms.",
                        Category.DORM_ESSENTIALS,
                        Condition.GOOD,
                        new BigDecimal("10.00"),
                        "Warren Hall Main Lobby",
                        now.plus(16, ChronoUnit.DAYS),
                        "/api/media/seed-fan.jpg",
                        "Quinn Murphy",
                        "murphq2@rpi.edu",
                        true
                )
        );

        try {
            edu.rpi.giveaway.auth.UserContext.setCurrentUser(new edu.rpi.giveaway.auth.UserPrincipal(
                    "system-seed", "seed@rpi.edu", "Campus Giveaway Admin", null
            ));
            for (CreatePostRequest req : seedRequests) {
                postService.createPost(req);
            }
        } finally {
            edu.rpi.giveaway.auth.UserContext.clear();
        }

        log.info("Successfully seeded {} posts into repository.", seedRequests.size());
    }
}
